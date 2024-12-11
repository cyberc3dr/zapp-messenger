package ru.sliva.zapp.data.database.user.sql

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object UserCredentialsTable : IntIdTable() {

    val login: Column<String> = varchar("login", length = 32)
    val password: Column<String> = varchar("password", length = 64)

}