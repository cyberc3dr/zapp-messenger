package ru.sliva.zapp.data.database.bot.sql

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object BotsTable : IntIdTable() {

    val name: Column<String> = text("name")
    val username: Column<String?> = varchar("username", length = 24).nullable()
    val description: Column<String?> = varchar("description", length = 64).nullable()
    val commands: Column<List<String>> = array("commands")
}