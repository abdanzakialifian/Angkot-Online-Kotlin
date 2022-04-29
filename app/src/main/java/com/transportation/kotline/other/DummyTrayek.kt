package com.transportation.kotline.other

import android.location.Location
import com.google.android.gms.maps.model.LatLng

object DummyTrayek {
    fun getTrayekAngkotA(destination: LatLng?, mLastLocation: Location?): String? {
        val destinationCustomer = Location("")
        destinationCustomer.latitude = destination?.latitude ?: 0.0
        destinationCustomer.longitude = destination?.longitude ?: 0.0

        val customerLocation = Location("")
        customerLocation.latitude = mLastLocation?.latitude ?: 0.0
        customerLocation.longitude = mLastLocation?.longitude ?: 0.0

        val jlVeteran = Location("")
        jlVeteran.latitude = -7.394799033719521
        jlVeteran.longitude = 109.700762915669

        val jlBrengkok = Location("")
        jlBrengkok.latitude = -7.396415762365111
        jlBrengkok.longitude = 109.70093516506572

        val jlCampurSalam = Location("")
        jlCampurSalam.latitude = -7.3966743624237745
        jlCampurSalam.longitude = 109.69889089680727

        val jlLetjendSuprapto = Location("")
        jlLetjendSuprapto.latitude = -7.398083949789377
        jlLetjendSuprapto.longitude = 109.69292715129609

        val jlMantrianom = Location("")
        jlMantrianom.latitude = -7.398732293120247
        jlMantrianom.longitude = 109.6283702594063

        val jlJendSoedirman = Location("")
        jlJendSoedirman.latitude = -7.405499073713895
        jlJendSoedirman.longitude = 109.60576501924116

        val jlMtHaryono = Location("")
        jlMtHaryono.latitude = -7.3955108893575945
        jlMtHaryono.longitude = 109.69891450257482

        val jlMayjendSoetojo = Location("")
        jlMayjendSoetojo.latitude = -7.392973866382533
        jlMayjendSoetojo.longitude = 109.69908275635157

        val jlLetnanKarjono = Location("")
        jlLetnanKarjono.latitude = -7.393295474003801
        jlLetnanKarjono.longitude = 109.7006343897887

        val jlStadion = Location("")
        jlStadion.latitude = -7.39199315369452
        jlStadion.longitude = 109.70438694635027

        val terminalBus = Location("")
        terminalBus.latitude = -7.392655920315736
        terminalBus.longitude = 109.70482103417467

        val customerToJlVeteran = customerLocation.distanceTo(jlVeteran) / 1000
        val customerToJlBrengkok = customerLocation.distanceTo(jlBrengkok) / 1000
        val customerToJlCampurSalam = customerLocation.distanceTo(jlCampurSalam) / 1000
        val customerToJlLetjendSuprapto = customerLocation.distanceTo(jlLetjendSuprapto) / 1000
        val customerToJlMantrianom = customerLocation.distanceTo(jlMantrianom) / 1000
        val customerToJlJendSoedirman = customerLocation.distanceTo(jlJendSoedirman) / 1000
        val customerToJlMtHaryono = customerLocation.distanceTo(jlMtHaryono) / 1000
        val customerToJlMayjendSoetojo = customerLocation.distanceTo(jlMayjendSoetojo) / 1000
        val customerToJlLetnanKarjono = customerLocation.distanceTo(jlLetnanKarjono) / 1000
        val customerToJlStadion = customerLocation.distanceTo(jlStadion) / 1000
        val customerToTerminal = customerLocation.distanceTo(terminalBus) / 1000

        if (customerToJlVeteran in 0F.rangeTo(0.17980675F) && customerToJlBrengkok in 0F.rangeTo(
                0.17980675F
            ) || customerToJlBrengkok in 0F.rangeTo(0.22749068F) && customerToJlCampurSalam in 0F.rangeTo(
                0.22749068F
            ) || customerToJlCampurSalam in 0F.rangeTo(0.67659587F) && customerToJlLetjendSuprapto in 0F.rangeTo(
                0.67659587F
            ) || customerToJlLetjendSuprapto in 0F.rangeTo(7.1273675F) && customerToJlMantrianom in 0F.rangeTo(
                7.1273675F
            ) || customerToJlMantrianom in 0F.rangeTo(2.6053631F) && customerToJlJendSoedirman in 0F.rangeTo(
                2.6053631F
            ) || customerToJlCampurSalam in 0F.rangeTo(0.12869798F) && customerToJlMtHaryono in 0F.rangeTo(
                0.12869798F
            ) || customerToJlMtHaryono in 0F.rangeTo(0.28119034F) && customerToJlMayjendSoetojo in 0F.rangeTo(
                0.28119034F
            ) || customerToJlMayjendSoetojo in 0F.rangeTo(0.17495409F) && customerToJlLetnanKarjono in 0F.rangeTo(
                0.17495409F
            ) || customerToJlLetnanKarjono in 0F.rangeTo(0.43860513F) && customerToJlStadion in 0F.rangeTo(
                0.43860513F
            ) || customerToJlStadion in 0F.rangeTo(0.08757355F) && customerToTerminal in 0F.rangeTo(
                0.08757355F
            )
        ) {
            return "A"
        } else {
            return null
        }
    }

    fun getTrayekDestinationA(destination: LatLng?): Boolean {
        val destinationCustomer = Location("")
        destinationCustomer.latitude = destination?.latitude ?: 0.0
        destinationCustomer.longitude = destination?.longitude ?: 0.0

        val jlVeteran = Location("")
        jlVeteran.latitude = -7.394799033719521
        jlVeteran.longitude = 109.700762915669

        val jlBrengkok = Location("")
        jlBrengkok.latitude = -7.396415762365111
        jlBrengkok.longitude = 109.70093516506572

        val jlCampurSalam = Location("")
        jlCampurSalam.latitude = -7.3966743624237745
        jlCampurSalam.longitude = 109.69889089680727

        val jlLetjendSuprapto = Location("")
        jlLetjendSuprapto.latitude = -7.398083949789377
        jlLetjendSuprapto.longitude = 109.69292715129609

        val jlMantrianom = Location("")
        jlMantrianom.latitude = -7.398732293120247
        jlMantrianom.longitude = 109.6283702594063

        val jlJendSoedirman = Location("")
        jlJendSoedirman.latitude = -7.405499073713895
        jlJendSoedirman.longitude = 109.60576501924116

        val jlMtHaryono = Location("")
        jlMtHaryono.latitude = -7.3955108893575945
        jlMtHaryono.longitude = 109.69891450257482

        val jlMayjendSoetojo = Location("")
        jlMayjendSoetojo.latitude = -7.392973866382533
        jlMayjendSoetojo.longitude = 109.69908275635157

        val jlLetnanKarjono = Location("")
        jlLetnanKarjono.latitude = -7.393295474003801
        jlLetnanKarjono.longitude = 109.7006343897887

        val jlStadion = Location("")
        jlStadion.latitude = -7.39199315369452
        jlStadion.longitude = 109.70438694635027

        val terminalBus = Location("")
        terminalBus.latitude = -7.392655920315736
        terminalBus.longitude = 109.70482103417467

        val destinationToJlVeteran = destinationCustomer.distanceTo(jlVeteran) / 1000
        val destinationToJlBrengkok = destinationCustomer.distanceTo(jlBrengkok) / 1000
        val destinationToJlCampurSalam = destinationCustomer.distanceTo(jlCampurSalam) / 1000
        val destinationToJlLetjendSuprapto =
            destinationCustomer.distanceTo(jlLetjendSuprapto) / 1000
        val destinationToJlMantrianom = destinationCustomer.distanceTo(jlMantrianom) / 1000
        val destinationToJlJendSoedirman = destinationCustomer.distanceTo(jlJendSoedirman) / 1000
        val destinationToJlMtHaryono = destinationCustomer.distanceTo(jlMtHaryono) / 1000
        val destinationToJlMayjendSoetojo = destinationCustomer.distanceTo(jlMayjendSoetojo) / 1000
        val destinationToJlLetnanKarjono = destinationCustomer.distanceTo(jlLetnanKarjono) / 1000
        val destinationToJlStadion = destinationCustomer.distanceTo(jlStadion) / 1000
        val destinationToTerminal = destinationCustomer.distanceTo(terminalBus) / 1000

        return destinationToJlVeteran in 0F.rangeTo(0.17980675F) && destinationToJlBrengkok in 0F.rangeTo(
            0.17980675F
        ) || destinationToJlBrengkok in 0F.rangeTo(0.22749068F) && destinationToJlCampurSalam in 0F.rangeTo(
            0.22749068F
        ) || destinationToJlCampurSalam in 0F.rangeTo(0.67659587F) && destinationToJlLetjendSuprapto in 0F.rangeTo(
            0.67659587F
        ) || destinationToJlLetjendSuprapto in 0F.rangeTo(7.1273675F) && destinationToJlMantrianom in 0F.rangeTo(
            7.1273675F
        ) || destinationToJlMantrianom in 0F.rangeTo(2.6053631F) && destinationToJlJendSoedirman in 0F.rangeTo(
            2.6053631F
        ) || destinationToJlCampurSalam in 0F.rangeTo(0.12869798F) && destinationToJlMtHaryono in 0F.rangeTo(
            0.12869798F
        ) || destinationToJlMtHaryono in 0F.rangeTo(0.28119034F) && destinationToJlMayjendSoetojo in 0F.rangeTo(
            0.28119034F
        ) || destinationToJlMayjendSoetojo in 0F.rangeTo(0.17495409F) && destinationToJlLetnanKarjono in 0F.rangeTo(
            0.17495409F
        ) || destinationToJlLetnanKarjono in 0F.rangeTo(0.43860513F) && destinationToJlStadion in 0F.rangeTo(
            0.43860513F
        ) || destinationToJlStadion in 0F.rangeTo(0.08757355F) && destinationToTerminal in 0F.rangeTo(
            0.08757355F
        )
    }
}