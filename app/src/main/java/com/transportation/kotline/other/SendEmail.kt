package com.transportation.kotline.other

import android.app.Application
import com.transportation.kotline.BuildConfig
import com.transportation.kotline.R
import papaya.`in`.sendmail.SendMail
import java.util.*


class SendEmail(val app: Application, val email: String) {
    // greetings
    private val calendar: Calendar = Calendar.getInstance()
    private val greetings = when (calendar[Calendar.HOUR_OF_DAY]) {
        in 5..12 -> {
            app.getString(R.string.good_morning)
        }
        in 12..17 -> {
            app.getString(R.string.good_afternoon)
        }
        else -> {
            app.getString(R.string.good_evening)
        }
    }

    fun email() {
        // send email to user
        val mail = SendMail(
            BuildConfig.EMAIL, BuildConfig.PASSWORD,
            email,
            app.getString(R.string.subjectMail),
            app.getString(R.string.messageMail, greetings)
        )
        mail.execute()
    }
}