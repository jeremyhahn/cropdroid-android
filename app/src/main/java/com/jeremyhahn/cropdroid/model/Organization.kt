package com.jeremyhahn.cropdroid.model

data class Organization(val id: Long, val name: String, val farms: ArrayList<Farm>, val license: String, var roles: ArrayList<String>)