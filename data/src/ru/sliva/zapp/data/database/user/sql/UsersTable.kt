package ru.sliva.zapp.data.database.user.sql

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object UsersTable : IntIdTable() {

    val name: Column<String> = text("name")
    val username: Column<String?> = varchar("username", length = 24).nullable()
    val description: Column<String?> = varchar("description", length = 64).nullable()

}