package com.jeremyhahn.cropdroid.model

data class Organization(val id: Int, val name: String, val farms: ArrayList<Farm>, val license: String, val roles: ArrayList<String>)