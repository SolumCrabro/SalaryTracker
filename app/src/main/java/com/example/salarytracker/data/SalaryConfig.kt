package com.example.salarytracker.data


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salary_configs")
data class SalaryConfig(
    @PrimaryKey val monthYearKey: String, // Ключ в формате "МЕСЯЦ_ГОД" (например, "ОКТЯБРЬ_2026")
    val customSalary: Double              // Измененная пользователем зарплата
)