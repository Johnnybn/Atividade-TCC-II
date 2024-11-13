package com.example.plandey

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.plandey.ui.theme.PlanDeyTheme
import java.util.Calendar
import java.util.Date

class MainActivity : ComponentActivity() {
    private var alarmManager: AlarmManager? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        setContent {
            PlanDeyTheme {
                MainScreen()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun MainScreen() {
        val currentCalendar = Calendar.getInstance()
        val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = currentCalendar.get(Calendar.MONTH)
        val currentYear = currentCalendar.get(Calendar.YEAR)

        var selectedMonth by remember { mutableStateOf(currentMonth) }
        var selectedYear by remember { mutableStateOf(currentYear) }
        var scheduledAppointments by remember { mutableStateOf(mutableListOf<String>()) }
        var showAppointmentList by remember { mutableStateOf(false) }
        var showAddAppointmentDialog by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { showAddAppointmentDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Novo Compromisso")
            }

            Spacer(modifier = Modifier.height(16.dp))

            CalendarView(
                currentDay = currentDay,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showAppointmentList = !showAppointmentList },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Listar Compromissos")
            }

            if (showAppointmentList) {
                AppointmentList(scheduledAppointments) { index ->
                    stopAlarmAndDeleteAppointment(index, scheduledAppointments)
                }
            }

            if (showAddAppointmentDialog) {
                AddAppointmentDialog(
                    onDismiss = { showAddAppointmentDialog = false },
                    onAppointmentAdded = { name, timeInMillis ->
                        val appointment = "Compromisso: $name - ${Date(timeInMillis)}"
                        scheduledAppointments.add(appointment)

                        scheduleAlarm(timeInMillis, scheduledAppointments.size - 1)
                        showAddAppointmentDialog = false
                    }
                )
            }
        }
    }

    @Composable
    fun AppointmentList(
        scheduledAppointments: List<String>,
        onDeleteAppointment: (Int) -> Unit
    ) {
        LazyColumn {
            items(scheduledAppointments.size) { index ->
                val appointment = scheduledAppointments[index]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appointment,
                        modifier = Modifier.weight(1f)
                    )

                    Button(onClick = {
                        onDeleteAppointment(index)
                    }) {
                        Text("Parar e Apagar")
                    }
                }
            }
        }
    }

    private fun items(size: Int, any: Any) {
        TODO("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun CalendarView(
        currentDay: Int,
        selectedMonth: Int,
        selectedYear: Int
    ) {
        val daysInMonth = getDaysInMonth(selectedYear, selectedMonth)
        val monthNames = listOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${monthNames[selectedMonth]} $selectedYear",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(16.dp)
            )

            Column {
                val daysPerWeek = 7
                for (weekStart in daysInMonth.indices step daysPerWeek) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (dayOffset in 0 until daysPerWeek) {
                            val dayIndex = weekStart + dayOffset
                            if (dayIndex < daysInMonth.size) {
                                val day = daysInMonth[dayIndex]
                                Text(
                                    text = day.toString(),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(32.dp),
                                    style = MaterialTheme.typography.body1
                                )
                            } else {
                                Spacer(modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getDaysInMonth(year: Int, month: Int): List<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..daysInMonth).toList()
    }

    @Composable
    fun AddAppointmentDialog(
        onDismiss: () -> Unit,
        onAppointmentAdded: (String, Long) -> Unit
    ) {
        var appointmentName by remember { mutableStateOf("") }
        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }
        val calendar = Calendar.getInstance()

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Novo Compromisso") },
            text = {
                Column {
                    TextField(
                        value = appointmentName,
                        onValueChange = { appointmentName = it },
                        label = { Text("Digite o nome do compromisso") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = { showDatePicker = true }) {
                        Text("Escolher Data")
                    }

                    if (showDatePicker) {
                        DatePickerDialog(
                            LocalContext.current,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                showDatePicker = false
                                showTimePicker = true
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }

                    if (showTimePicker) {
                        TimePickerDialog(
                            LocalContext.current,
                            { _, hourOfDay, minute ->
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                calendar.set(Calendar.MINUTE, minute)
                                showTimePicker = false
                                onAppointmentAdded(appointmentName, calendar.timeInMillis)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(timeInMillis: Long, appointmentId: Int) {
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, appointmentId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }

    private fun stopAlarmAndDeleteAppointment(
        appointmentId: Int,
        scheduledAppointments: MutableList<String>
    ) {
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, appointmentId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.cancel(pendingIntent)

        if (appointmentId < scheduledAppointments.size) {
            scheduledAppointments.removeAt(appointmentId)
        }
    }
}
