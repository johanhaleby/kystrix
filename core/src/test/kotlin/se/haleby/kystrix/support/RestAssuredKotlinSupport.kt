package se.haleby.kystrix.support

import io.restassured.response.Response


inline fun <reified T : Any> Response.asJson(): T = this.`as`(T::class.java)