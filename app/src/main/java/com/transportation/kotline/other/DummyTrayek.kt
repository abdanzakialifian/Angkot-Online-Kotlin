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

        // menghitung jarak antara jl veteran dan jl brengkok, ketemu hasil lalu cek apakah user berada pada jarak tersebut
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

    fun getTrayekAngkotB(mLastLocation: Location?): String? {

        val customerLocation = Location("")
        customerLocation.latitude = mLastLocation?.latitude ?: 0.0
        customerLocation.longitude = mLastLocation?.longitude ?: 0.0

        val jlVeteran = Location("")
        jlVeteran.latitude = -7.393286634547339
        jlVeteran.longitude = 109.70061167423222

        val jlLetnanKarjono = Location("")
        jlLetnanKarjono.latitude = -7.391957814455376
        jlLetnanKarjono.longitude = 109.70436277113605

        val jlStadion = Location("")
        jlStadion.latitude = -7.393062008311444
        jlStadion.longitude = 109.70493209729725

        val jlAjibarangSecang = Location("")
        jlAjibarangSecang.latitude = -7.3944717509148825
        jlAjibarangSecang.longitude = 109.70561317241973

        val jlCampurSalam = Location("")
        jlCampurSalam.latitude = -7.3966743624237745
        jlCampurSalam.longitude = 109.69889089680727

        val jlMtHaryono = Location("")
        jlMtHaryono.latitude = -7.395230861464321
        jlMtHaryono.longitude = 109.69893597195728

        val jlMayjendSoetojo = Location("")
        jlMayjendSoetojo.latitude = -7.3929677430276355
        jlMayjendSoetojo.longitude = 109.69907814958101

        val jlGotongRoyong = Location("")
        jlGotongRoyong.latitude = -7.392679466782409
        jlGotongRoyong.longitude = 109.69252086138916

        val jlAlMunawwaroh = Location("")
        jlAlMunawwaroh.latitude = -7.392585836782173
        jlAlMunawwaroh.longitude = 109.6900924200438

        val jlPasarWage = Location("")
        jlPasarWage.latitude = -7.399586906299058
        jlPasarWage.longitude = 109.68603285296724

        val jlBrengkok = Location("")
        jlBrengkok.latitude = -7.397107412864937
        jlBrengkok.longitude = 109.69303324895357

        val jlSingamerta = Location("")
        jlSingamerta.latitude = -7.39133610612604
        jlSingamerta.longitude = 109.74286100424477

        val jlMadukara = Location("")
        jlMadukara.latitude = -7.379043887211778
        jlMadukara.longitude = 109.74609622769049

        val jlRejasa = Location("")
        jlRejasa.latitude = -7.387036588738758
        jlRejasa.longitude = 109.690708412322

        val jlSunanGripit = Location("")
        jlSunanGripit.latitude = -7.392681363604244
        jlSunanGripit.longitude = 109.69503606555965

        val customerToJlVeteran = customerLocation.distanceTo(jlVeteran) / 1000
        val customerToJlLetnanKarjono = customerLocation.distanceTo(jlLetnanKarjono) / 1000
        val customerToJlStadion = customerLocation.distanceTo(jlStadion) / 1000
        val customerToJlAjibarangSecang = customerLocation.distanceTo(jlAjibarangSecang) / 1000
        val customerToJlCampurSalam = customerLocation.distanceTo(jlCampurSalam) / 1000
        val customerToJlMtHaryono = customerLocation.distanceTo(jlMtHaryono) / 1000
        val customerToJlMayjendSoetojo = customerLocation.distanceTo(jlMayjendSoetojo) / 1000
        val customerToJlGotongRoyong = customerLocation.distanceTo(jlGotongRoyong) / 1000
        val customerToJlAlMunawwaroh = customerLocation.distanceTo(jlAlMunawwaroh) / 1000
        val customerToJlPasarWage = customerLocation.distanceTo(jlPasarWage) / 1000
        val customerToJlBrengkok = customerLocation.distanceTo(jlBrengkok) / 1000
        val customerToJlSingamerta = customerLocation.distanceTo(jlSingamerta) / 1000
        val customerToJlMadukara = customerLocation.distanceTo(jlMadukara) / 1000
        val customerToJlRejasa = customerLocation.distanceTo(jlRejasa) / 1000
        val customerToJlSunanGripit = customerLocation.distanceTo(jlSunanGripit) / 1000

        if (customerToJlVeteran in 0F.rangeTo(0.43942437F) && customerToJlLetnanKarjono in 0F.rangeTo(
                0.43942437F
            ) || customerToJlLetnanKarjono in 0F.rangeTo(0.1373421F) && customerToJlStadion in 0F.rangeTo(
                0.1373421F
            ) || customerToJlStadion in 0F.rangeTo(0.17309159F) && customerToJlAjibarangSecang in 0F.rangeTo(
                0.17309159F
            ) || customerToJlAjibarangSecang in 0F.rangeTo(0.78109133F) && customerToJlCampurSalam in 0F.rangeTo(
                0.78109133F
            ) || customerToJlCampurSalam in 0F.rangeTo(0.15971817F) && customerToJlMtHaryono in 0F.rangeTo(
                0.15971817F
            ) || customerToJlMtHaryono in 0F.rangeTo(0.25077602F) && customerToJlMayjendSoetojo in 0F.rangeTo(
                0.25077602F
            ) || customerToJlMayjendSoetojo in 0F.rangeTo(0.72462785F) && customerToJlGotongRoyong in 0F.rangeTo(
                0.72462785F
            ) || customerToJlGotongRoyong in 0F.rangeTo(0.2683005F) && customerToJlAlMunawwaroh in 0F.rangeTo(
                0.2683005F
            ) || customerToJlAlMunawwaroh in 0F.rangeTo(0.89462245F) && customerToJlPasarWage in 0F.rangeTo(
                0.89462245F
            ) || customerToJlPasarWage in 0F.rangeTo(0.8200417F) && customerToJlBrengkok in 0F.rangeTo(
                0.8200417F
            ) || customerToJlBrengkok in 0F.rangeTo(5.5378833F) && customerToJlSingamerta in 0F.rangeTo(
                5.5378833F
            ) || customerToJlSingamerta in 0F.rangeTo(1.4055679F) && customerToJlMadukara in 0F.rangeTo(
                1.4055679F
            ) || customerToJlMadukara in 0F.rangeTo(6.17852F) && customerToJlRejasa in 0F.rangeTo(
                6.17852F
            ) || customerToJlRejasa in 0F.rangeTo(0.78612006F) && customerToJlSunanGripit in 0F.rangeTo(
                0.78612006F
            )
        ) {
            return "B"
        } else {
            return null
        }
    }

    fun getTrayekDestinationB(destination: LatLng?): Boolean {
        val destinationCustomer = Location("")
        destinationCustomer.latitude = destination?.latitude ?: 0.0
        destinationCustomer.longitude = destination?.longitude ?: 0.0

        val jlVeteran = Location("")
        jlVeteran.latitude = -7.393286634547339
        jlVeteran.longitude = 109.70061167423222

        val jlLetnanKarjono = Location("")
        jlLetnanKarjono.latitude = -7.391957814455376
        jlLetnanKarjono.longitude = 109.70436277113605

        val jlStadion = Location("")
        jlStadion.latitude = -7.393062008311444
        jlStadion.longitude = 109.70493209729725

        val jlAjibarangSecang = Location("")
        jlAjibarangSecang.latitude = -7.3944717509148825
        jlAjibarangSecang.longitude = 109.70561317241973

        val jlCampurSalam = Location("")
        jlCampurSalam.latitude = -7.3966743624237745
        jlCampurSalam.longitude = 109.69889089680727

        val jlMtHaryono = Location("")
        jlMtHaryono.latitude = -7.395230861464321
        jlMtHaryono.longitude = 109.69893597195728

        val jlMayjendSoetojo = Location("")
        jlMayjendSoetojo.latitude = -7.3929677430276355
        jlMayjendSoetojo.longitude = 109.69907814958101

        val jlGotongRoyong = Location("")
        jlGotongRoyong.latitude = -7.392679466782409
        jlGotongRoyong.longitude = 109.69252086138916

        val jlAlMunawwaroh = Location("")
        jlAlMunawwaroh.latitude = -7.392585836782173
        jlAlMunawwaroh.longitude = 109.6900924200438

        val jlPasarWage = Location("")
        jlPasarWage.latitude = -7.399586906299058
        jlPasarWage.longitude = 109.68603285296724

        val jlBrengkok = Location("")
        jlBrengkok.latitude = -7.397107412864937
        jlBrengkok.longitude = 109.69303324895357

        val jlSingamerta = Location("")
        jlSingamerta.latitude = -7.39133610612604
        jlSingamerta.longitude = 109.74286100424477

        val jlMadukara = Location("")
        jlMadukara.latitude = -7.379043887211778
        jlMadukara.longitude = 109.74609622769049

        val jlRejasa = Location("")
        jlRejasa.latitude = -7.387036588738758
        jlRejasa.longitude = 109.690708412322

        val jlSunanGripit = Location("")
        jlSunanGripit.latitude = -7.392681363604244
        jlSunanGripit.longitude = 109.69503606555965

        val destinationToJlVeteran = destinationCustomer.distanceTo(jlVeteran) / 1000
        val destinationToJlLetnanKarjono = destinationCustomer.distanceTo(jlLetnanKarjono) / 1000
        val destinationToJlStadion = destinationCustomer.distanceTo(jlStadion) / 1000
        val destinationToJlAjibarangSecang =
            destinationCustomer.distanceTo(jlAjibarangSecang) / 1000
        val destinationToJlCampurSalam = destinationCustomer.distanceTo(jlCampurSalam) / 1000
        val destinationToJlMtHaryono = destinationCustomer.distanceTo(jlMtHaryono) / 1000
        val destinationToJlMayjendSoetojo = destinationCustomer.distanceTo(jlMayjendSoetojo) / 1000
        val destinationToJlGotongRoyong = destinationCustomer.distanceTo(jlGotongRoyong) / 1000
        val destinationToJlAlMunawwaroh = destinationCustomer.distanceTo(jlAlMunawwaroh) / 1000
        val destinationToJlPasarWage = destinationCustomer.distanceTo(jlPasarWage) / 1000
        val destinationToJlBrengkok = destinationCustomer.distanceTo(jlBrengkok) / 1000
        val destinationToJlSingamerta = destinationCustomer.distanceTo(jlSingamerta) / 1000
        val destinationToJlMadukara = destinationCustomer.distanceTo(jlMadukara) / 1000
        val destinationToJlRejasa = destinationCustomer.distanceTo(jlRejasa) / 1000
        val destinationToJlSunanGripit = destinationCustomer.distanceTo(jlSunanGripit) / 1000

        return destinationToJlVeteran in 0F.rangeTo(0.43942437F) && destinationToJlLetnanKarjono in 0F.rangeTo(
            0.43942437F
        ) || destinationToJlLetnanKarjono in 0F.rangeTo(0.1373421F) && destinationToJlStadion in 0F.rangeTo(
            0.1373421F
        ) || destinationToJlStadion in 0F.rangeTo(0.17309159F) && destinationToJlAjibarangSecang in 0F.rangeTo(
            0.17309159F
        ) || destinationToJlAjibarangSecang in 0F.rangeTo(0.78109133F) && destinationToJlCampurSalam in 0F.rangeTo(
            0.78109133F
        ) || destinationToJlCampurSalam in 0F.rangeTo(0.15971817F) && destinationToJlMtHaryono in 0F.rangeTo(
            0.15971817F
        ) || destinationToJlMtHaryono in 0F.rangeTo(0.25077602F) && destinationToJlMayjendSoetojo in 0F.rangeTo(
            0.25077602F
        ) || destinationToJlMayjendSoetojo in 0F.rangeTo(0.72462785F) && destinationToJlGotongRoyong in 0F.rangeTo(
            0.72462785F
        ) || destinationToJlGotongRoyong in 0F.rangeTo(0.2683005F) && destinationToJlAlMunawwaroh in 0F.rangeTo(
            0.2683005F
        ) || destinationToJlAlMunawwaroh in 0F.rangeTo(0.89462245F) && destinationToJlPasarWage in 0F.rangeTo(
            0.89462245F
        ) || destinationToJlPasarWage in 0F.rangeTo(0.8200417F) && destinationToJlBrengkok in 0F.rangeTo(
            0.8200417F
        ) || destinationToJlBrengkok in 0F.rangeTo(5.5378833F) && destinationToJlSingamerta in 0F.rangeTo(
            5.5378833F
        ) || destinationToJlSingamerta in 0F.rangeTo(1.4055679F) && destinationToJlMadukara in 0F.rangeTo(
            1.4055679F
        ) || destinationToJlMadukara in 0F.rangeTo(6.17852F) && destinationToJlRejasa in 0F.rangeTo(
            6.17852F
        ) || destinationToJlRejasa in 0F.rangeTo(0.78612006F) && destinationToJlSunanGripit in 0F.rangeTo(
            0.78612006F
        )
    }

    fun getTrayekAngkotC(mLastLocation: Location?): String? {

        val customerLocation = Location("")
        customerLocation.latitude = mLastLocation?.latitude ?: 0.0
        customerLocation.longitude = mLastLocation?.longitude ?: 0.0

        val jlVeteran = Location("")
        jlVeteran.latitude = -7.394840142095557
        jlVeteran.longitude = 109.70068781164427

        val jlCampurSalam = Location("")
        jlCampurSalam.latitude = -7.3966743624237745
        jlCampurSalam.longitude = 109.69889089680727

        val jlLetjendSuprapto = Location("")
        jlLetjendSuprapto.latitude = -7.399562570467833
        jlLetjendSuprapto.longitude = 109.68604454812801

        val jlAlmunawwaroh = Location("")
        jlAlmunawwaroh.latitude = -7.395031113044718
        jlAlmunawwaroh.longitude = 109.69012034651269

        val jlGotongRoyong = Location("")
        jlGotongRoyong.latitude = -7.392543046575512
        jlGotongRoyong.longitude = 109.69011758939891

        val jlSunanGripit = Location("")
        jlSunanGripit.latitude = -7.392493680039571
        jlSunanGripit.longitude = 109.69502404608019

        val jlPetambakan = Location("")
        jlPetambakan.latitude = -7.385932436549343
        jlPetambakan.longitude = 109.69113398260461

        val jlBanjarmangu = Location("")
        jlBanjarmangu.latitude = -7.359293959744121
        jlBanjarmangu.longitude = 109.6918696216978

        val polsekBanjarmangu = Location("")
        polsekBanjarmangu.latitude = -7.36054271148776
        polsekBanjarmangu.longitude = 109.68800802062427

        val jlKiJagapati = Location("")
        jlKiJagapati.latitude = -7.391222574387803
        jlKiJagapati.longitude = 109.69513972955063

        val jlMayjendSoetojo = Location("")
        jlMayjendSoetojo.latitude = -7.3929782667728965
        jlMayjendSoetojo.longitude = 109.70048342573183

        val jlMtHaryono = Location("")
        jlMtHaryono.latitude = -7.395230861464321
        jlMtHaryono.longitude = 109.69893597195728

        val customerToJlVeteran = customerLocation.distanceTo(jlVeteran) / 1000
        val customerToJlCampurSalam = customerLocation.distanceTo(jlCampurSalam) / 1000
        val customerToJlLetjendSupraptop = customerLocation.distanceTo(jlLetjendSuprapto) / 1000
        val customerToJlAlmunawwaroh = customerLocation.distanceTo(jlAlmunawwaroh) / 1000
        val customerToJlGotongRoyong = customerLocation.distanceTo(jlGotongRoyong) / 1000
        val customerToJlSunanGripit = customerLocation.distanceTo(jlSunanGripit) / 1000
        val customerToJlPetambakan = customerLocation.distanceTo(jlPetambakan) / 1000
        val customerToJlBanjarmangu = customerLocation.distanceTo(jlBanjarmangu) / 1000
        val customerToPolsekBanjarmangu = customerLocation.distanceTo(polsekBanjarmangu) / 1000
        val customerToJlKiJagapati = customerLocation.distanceTo(jlKiJagapati) / 1000
        val customerToJlMayjendSoetojo = customerLocation.distanceTo(jlMayjendSoetojo) / 1000
        val customerToJlMtHaryono = customerLocation.distanceTo(jlMtHaryono) / 1000

        if (customerToJlVeteran in 0F.rangeTo(0.28372997F) && customerToJlCampurSalam in 0F.rangeTo(
                0.28372997F
            ) || customerToJlCampurSalam in 0F.rangeTo(1.4537477F) && customerToJlLetjendSupraptop in 0F.rangeTo(
                1.4537477F
            ) || customerToJlLetjendSupraptop in 0F.rangeTo(0.67350984F) && customerToJlAlmunawwaroh in 0F.rangeTo(
                0.67350984F
            ) || customerToJlAlmunawwaroh in 0F.rangeTo(0.27516207F) && customerToJlGotongRoyong in 0F.rangeTo(
                0.27516207F
            ) || customerToJlGotongRoyong in 0F.rangeTo(0.5417019F) && customerToJlSunanGripit in 0F.rangeTo(
                0.5417019F
            ) || customerToJlSunanGripit in 0F.rangeTo(0.84319293F) && customerToJlPetambakan in 0F.rangeTo(
                0.84319293F
            ) || customerToJlPetambakan in 0F.rangeTo(2.9471366F) && customerToJlBanjarmangu in 0F.rangeTo(
                2.9471366F
            ) || customerToJlBanjarmangu in 0F.rangeTo(0.4481622F) && customerToPolsekBanjarmangu in 0F.rangeTo(
                0.4481622F
            ) || customerToPolsekBanjarmangu in 0F.rangeTo(3.4831262F) && customerToJlKiJagapati in 0F.rangeTo(
                3.4831262F
            ) || customerToJlKiJagapati in 0F.rangeTo(0.6210776F) && customerToJlMayjendSoetojo in 0F.rangeTo(
                0.6210776F
            ) || customerToJlMayjendSoetojo in 0F.rangeTo(1.1454027F) && customerToJlGotongRoyong in 0F.rangeTo(
                1.1454027F
            ) || customerToJlGotongRoyong in 0F.rangeTo(0.27516207F) && customerToJlAlmunawwaroh in 0F.rangeTo(
                0.27516207F
            ) || customerToJlAlmunawwaroh in 0F.rangeTo(0.67350984F) && customerToJlLetjendSupraptop in 0F.rangeTo(
                0.67350984F
            ) || customerToJlLetjendSupraptop in 0F.rangeTo(1.4537477F) && customerToJlCampurSalam in 0F.rangeTo(
                1.4537477F
            ) || customerToJlCampurSalam in 0F.rangeTo(0.15971817F) && customerToJlMtHaryono in 0F.rangeTo(
                0.15971817F
            ) || customerToJlMtHaryono in 0F.rangeTo(0.302071F) && customerToJlMayjendSoetojo in 0F.rangeTo(
                0.302071F
            ) || customerToJlMayjendSoetojo in 0F.rangeTo(0.2071424F) && customerToJlVeteran in 0F.rangeTo(
                0.2071424F
            )
        ) {
            return "C"
        } else {
            return null
        }
    }

    fun getTrayekDestinationC(destination: LatLng?): Boolean {
        val destinationCustomer = Location("")
        destinationCustomer.latitude = destination?.latitude ?: 0.0
        destinationCustomer.longitude = destination?.longitude ?: 0.0

        val jlVeteran = Location("")
        jlVeteran.latitude = -7.394840142095557
        jlVeteran.longitude = 109.70068781164427

        val jlCampurSalam = Location("")
        jlCampurSalam.latitude = -7.3966743624237745
        jlCampurSalam.longitude = 109.69889089680727

        val jlLetjendSuprapto = Location("")
        jlLetjendSuprapto.latitude = -7.399562570467833
        jlLetjendSuprapto.longitude = 109.68604454812801

        val jlAlmunawwaroh = Location("")
        jlAlmunawwaroh.latitude = -7.395031113044718
        jlAlmunawwaroh.longitude = 109.69012034651269

        val jlGotongRoyong = Location("")
        jlGotongRoyong.latitude = -7.392543046575512
        jlGotongRoyong.longitude = 109.69011758939891

        val jlSunanGripit = Location("")
        jlSunanGripit.latitude = -7.392493680039571
        jlSunanGripit.longitude = 109.69502404608019

        val jlPetambakan = Location("")
        jlPetambakan.latitude = -7.385932436549343
        jlPetambakan.longitude = 109.69113398260461

        val jlBanjarmangu = Location("")
        jlBanjarmangu.latitude = -7.359293959744121
        jlBanjarmangu.longitude = 109.6918696216978

        val polsekBanjarmangu = Location("")
        polsekBanjarmangu.latitude = -7.36054271148776
        polsekBanjarmangu.longitude = 109.68800802062427

        val jlKiJagapati = Location("")
        jlKiJagapati.latitude = -7.391222574387803
        jlKiJagapati.longitude = 109.69513972955063

        val jlMayjendSoetojo = Location("")
        jlMayjendSoetojo.latitude = -7.3929782667728965
        jlMayjendSoetojo.longitude = 109.70048342573183

        val jlMtHaryono = Location("")
        jlMtHaryono.latitude = -7.395230861464321
        jlMtHaryono.longitude = 109.69893597195728

        val destinationToJlVeteran = destinationCustomer.distanceTo(jlVeteran) / 1000
        val destinationToJlCampurSalam = destinationCustomer.distanceTo(jlCampurSalam) / 1000
        val destinationToJlLetjendSupraptop =
            destinationCustomer.distanceTo(jlLetjendSuprapto) / 1000
        val destinationToJlAlmunawwaroh = destinationCustomer.distanceTo(jlAlmunawwaroh) / 1000
        val destinationToJlGotongRoyong = destinationCustomer.distanceTo(jlGotongRoyong) / 1000
        val destinationToJlSunanGripit = destinationCustomer.distanceTo(jlSunanGripit) / 1000
        val destinationToJlPetambakan = destinationCustomer.distanceTo(jlPetambakan) / 1000
        val destinationToJlBanjarmangu = destinationCustomer.distanceTo(jlBanjarmangu) / 1000
        val destinationToPolsekBanjarmangu =
            destinationCustomer.distanceTo(polsekBanjarmangu) / 1000
        val destinationToJlKiJagapati = destinationCustomer.distanceTo(jlKiJagapati) / 1000
        val destinationToJlMayjendSoetojo = destinationCustomer.distanceTo(jlMayjendSoetojo) / 1000
        val destinationToJlMtHaryono = destinationCustomer.distanceTo(jlMtHaryono) / 1000

        return destinationToJlVeteran in 0F.rangeTo(0.28372997F) && destinationToJlCampurSalam in 0F.rangeTo(
            0.28372997F
        ) || destinationToJlCampurSalam in 0F.rangeTo(1.4537477F) && destinationToJlLetjendSupraptop in 0F.rangeTo(
            1.4537477F
        ) || destinationToJlLetjendSupraptop in 0F.rangeTo(0.67350984F) && destinationToJlAlmunawwaroh in 0F.rangeTo(
            0.67350984F
        ) || destinationToJlAlmunawwaroh in 0F.rangeTo(0.27516207F) && destinationToJlGotongRoyong in 0F.rangeTo(
            0.27516207F
        ) || destinationToJlGotongRoyong in 0F.rangeTo(0.5417019F) && destinationToJlSunanGripit in 0F.rangeTo(
            0.5417019F
        ) || destinationToJlSunanGripit in 0F.rangeTo(0.84319293F) && destinationToJlPetambakan in 0F.rangeTo(
            0.84319293F
        ) || destinationToJlPetambakan in 0F.rangeTo(2.9471366F) && destinationToJlBanjarmangu in 0F.rangeTo(
            2.9471366F
        ) || destinationToJlBanjarmangu in 0F.rangeTo(0.4481622F) && destinationToPolsekBanjarmangu in 0F.rangeTo(
            0.4481622F
        ) || destinationToPolsekBanjarmangu in 0F.rangeTo(3.4831262F) && destinationToJlKiJagapati in 0F.rangeTo(
            3.4831262F
        ) || destinationToJlKiJagapati in 0F.rangeTo(0.6210776F) && destinationToJlMayjendSoetojo in 0F.rangeTo(
            0.6210776F
        ) || destinationToJlMayjendSoetojo in 0F.rangeTo(1.1454027F) && destinationToJlGotongRoyong in 0F.rangeTo(
            1.1454027F
        ) || destinationToJlGotongRoyong in 0F.rangeTo(0.27516207F) && destinationToJlAlmunawwaroh in 0F.rangeTo(
            0.27516207F
        ) || destinationToJlAlmunawwaroh in 0F.rangeTo(0.67350984F) && destinationToJlLetjendSupraptop in 0F.rangeTo(
            0.67350984F
        ) || destinationToJlLetjendSupraptop in 0F.rangeTo(1.4537477F) && destinationToJlCampurSalam in 0F.rangeTo(
            1.4537477F
        ) || destinationToJlCampurSalam in 0F.rangeTo(0.15971817F) && destinationToJlMtHaryono in 0F.rangeTo(
            0.15971817F
        ) || destinationToJlMtHaryono in 0F.rangeTo(0.302071F) && destinationToJlMayjendSoetojo in 0F.rangeTo(
            0.302071F
        ) || destinationToJlMayjendSoetojo in 0F.rangeTo(0.2071424F) && destinationToJlVeteran in 0F.rangeTo(
            0.2071424F
        )
    }

    fun getTrayekAngkotD(mLastLocation: Location?): String? {

        val customerLocation = Location("")
        customerLocation.latitude = mLastLocation?.latitude ?: 0.0
        customerLocation.longitude = mLastLocation?.longitude ?: 0.0

        val jlVeteran = Location("")
        jlVeteran.latitude = -7.393286634547339
        jlVeteran.longitude = 109.70061167423222

        val jlLetnanKarjono = Location("")
        jlLetnanKarjono.latitude = -7.391957814455376
        jlLetnanKarjono.longitude = 109.70436277113605

        val jlStadion = Location("")
        jlStadion.latitude = -7.393062008311444
        jlStadion.longitude = 109.70493209729725

        val jlAjibarangSecang = Location("")
        jlAjibarangSecang.latitude = -7.3944717509148825
        jlAjibarangSecang.longitude = 109.70561317241973

        val jlCampurSalam = Location("")
        jlCampurSalam.latitude = -7.3966743624237745
        jlCampurSalam.longitude = 109.69889089680727

        val jlMtHaryono = Location("")
        jlMtHaryono.latitude = -7.395230861464321
        jlMtHaryono.longitude = 109.69893597195728

        val jlMayjendSoetojo = Location("")
        jlMayjendSoetojo.latitude = -7.3929677430276355
        jlMayjendSoetojo.longitude = 109.69907814958101

        val jlSunanGripit = Location("")
        jlSunanGripit.latitude = -7.3925535821418125
        jlSunanGripit.longitude = 109.6950338758701

        val jlRejasa = Location("")
        jlRejasa.latitude = -7.387036260108403
        jlRejasa.longitude = 109.69070857427552

        val jlMadukara = Location("")
        jlMadukara.latitude = -7.379043887211778
        jlMadukara.longitude = 109.74609622769049

        val jlSingamerta = Location("")
        jlSingamerta.latitude = -7.391250422958347
        jlSingamerta.longitude = 109.74284967730786

        val jlLetjendSuprapto = Location("")
        jlLetjendSuprapto.latitude = -7.399571113858571
        jlLetjendSuprapto.longitude = 109.68601952525529

        val jlAlMunawwaroh = Location("")
        jlAlMunawwaroh.latitude = -7.395129593968356
        jlAlMunawwaroh.longitude = 109.69013935139593

        val jlGotongRoyong = Location("")
        jlGotongRoyong.latitude = -7.392545545002634
        jlGotongRoyong.longitude = 109.69011664584171

        val customerToJlVeteran = customerLocation.distanceTo(jlVeteran) / 1000
        val customerToJlLetnanKarjono = customerLocation.distanceTo(jlLetnanKarjono) / 1000
        val customerToJlStadion = customerLocation.distanceTo(jlStadion) / 1000
        val customerToJlAjibarangSecang = customerLocation.distanceTo(jlAjibarangSecang) / 1000
        val customerToJlCampurSalam = customerLocation.distanceTo(jlCampurSalam) / 1000
        val customerToJlMtHaryono = customerLocation.distanceTo(jlMtHaryono) / 1000
        val customerToJlMayjendSoetojo = customerLocation.distanceTo(jlMayjendSoetojo) / 1000
        val customerToJlSunanGripit = customerLocation.distanceTo(jlSunanGripit) / 1000
        val customerToJlRejasa = customerLocation.distanceTo(jlRejasa) / 1000
        val customerToJlMadukara = customerLocation.distanceTo(jlMadukara) / 1000
        val customerToJlSingamerta = customerLocation.distanceTo(jlSingamerta) / 1000
        val customerToJlLetjendSuprapto = customerLocation.distanceTo(jlLetjendSuprapto) / 1000
        val customerToJlAlMunawwaroh = customerLocation.distanceTo(jlAlMunawwaroh) / 1000
        val customerToJlGotongRoyong = customerLocation.distanceTo(jlGotongRoyong) / 1000

        if (customerToJlVeteran in 0F.rangeTo(0.43942437F) && customerToJlLetnanKarjono in 0F.rangeTo(
                0.43942437F
            ) || customerToJlLetnanKarjono in 0F.rangeTo(0.1373421F) && customerToJlStadion in 0F.rangeTo(
                0.1373421F
            ) || customerToJlStadion in 0F.rangeTo(0.17309159F) && customerToJlAjibarangSecang in 0F.rangeTo(
                0.17309159F
            ) || customerToJlAjibarangSecang in 0F.rangeTo(0.78109133F) && customerToJlCampurSalam in 0F.rangeTo(
                0.78109133F
            ) || customerToJlCampurSalam in 0F.rangeTo(0.15971817F) && customerToJlMtHaryono in 0F.rangeTo(
                0.15971817F
            ) || customerToJlMtHaryono in 0F.rangeTo(0.25077602F) && customerToJlMayjendSoetojo in 0F.rangeTo(
                0.25077602F
            ) || customerToJlMayjendSoetojo in 0F.rangeTo(0.44883206F) && customerToJlSunanGripit in 0F.rangeTo(
                0.44883206F
            ) || customerToJlSunanGripit in 0F.rangeTo(0.77481407F) && customerToJlRejasa in 0F.rangeTo(
                0.77481407F
            ) || customerToJlRejasa in 0F.rangeTo(6.178497F) && customerToJlMadukara in 0F.rangeTo(
                6.178497F
            ) || customerToJlMadukara in 0F.rangeTo(1.3967254F) && customerToJlSingamerta in 0F.rangeTo(
                1.3967254F
            ) || customerToJlSingamerta in 0F.rangeTo(6.34115F) && customerToJlLetjendSuprapto in 0F.rangeTo(
                6.34115F
            ) || customerToJlLetjendSuprapto in 0F.rangeTo(0.6694348F) && customerToJlAlMunawwaroh in 0F.rangeTo(
                0.6694348F
            ) || customerToJlAlMunawwaroh in 0F.rangeTo(0.28578788F) && customerToJlGotongRoyong in 0F.rangeTo(
                0.28578788F
            ) || customerToJlGotongRoyong in 0F.rangeTo(0.9904535F) && customerToJlMayjendSoetojo in 0F.rangeTo(
                0.9904535F
            ) || customerToJlMayjendSoetojo in 0F.rangeTo(0.1729356F) && customerToJlVeteran in 0F.rangeTo(
                0.1729356F
            )
        ) {
            return "D"
        } else {
            return null
        }
    }

    fun getTrayekDestinationD(destination: LatLng?): Boolean {
        val destinationCustomer = Location("")
        destinationCustomer.latitude = destination?.latitude ?: 0.0
        destinationCustomer.longitude = destination?.longitude ?: 0.0

        val jlVeteran = Location("")
        jlVeteran.latitude = -7.393286634547339
        jlVeteran.longitude = 109.70061167423222

        val jlLetnanKarjono = Location("")
        jlLetnanKarjono.latitude = -7.391957814455376
        jlLetnanKarjono.longitude = 109.70436277113605

        val jlStadion = Location("")
        jlStadion.latitude = -7.393062008311444
        jlStadion.longitude = 109.70493209729725

        val jlAjibarangSecang = Location("")
        jlAjibarangSecang.latitude = -7.3944717509148825
        jlAjibarangSecang.longitude = 109.70561317241973

        val jlCampurSalam = Location("")
        jlCampurSalam.latitude = -7.3966743624237745
        jlCampurSalam.longitude = 109.69889089680727

        val jlMtHaryono = Location("")
        jlMtHaryono.latitude = -7.395230861464321
        jlMtHaryono.longitude = 109.69893597195728

        val jlMayjendSoetojo = Location("")
        jlMayjendSoetojo.latitude = -7.3929677430276355
        jlMayjendSoetojo.longitude = 109.69907814958101

        val jlSunanGripit = Location("")
        jlSunanGripit.latitude = -7.3925535821418125
        jlSunanGripit.longitude = 109.6950338758701

        val jlRejasa = Location("")
        jlRejasa.latitude = -7.387036260108403
        jlRejasa.longitude = 109.69070857427552

        val jlMadukara = Location("")
        jlMadukara.latitude = -7.379043887211778
        jlMadukara.longitude = 109.74609622769049

        val jlSingamerta = Location("")
        jlSingamerta.latitude = -7.391250422958347
        jlSingamerta.longitude = 109.74284967730786

        val jlLetjendSuprapto = Location("")
        jlLetjendSuprapto.latitude = -7.399571113858571
        jlLetjendSuprapto.longitude = 109.68601952525529

        val jlAlMunawwaroh = Location("")
        jlAlMunawwaroh.latitude = -7.395129593968356
        jlAlMunawwaroh.longitude = 109.69013935139593

        val jlGotongRoyong = Location("")
        jlGotongRoyong.latitude = -7.392545545002634
        jlGotongRoyong.longitude = 109.69011664584171

        val destinationToJlVeteran = destinationCustomer.distanceTo(jlVeteran) / 1000
        val destinationToJlLetnanKarjono = destinationCustomer.distanceTo(jlLetnanKarjono) / 1000
        val destinationToJlStadion = destinationCustomer.distanceTo(jlStadion) / 1000
        val destinationToJlAjibarangSecang =
            destinationCustomer.distanceTo(jlAjibarangSecang) / 1000
        val destinationToJlCampurSalam = destinationCustomer.distanceTo(jlCampurSalam) / 1000
        val destinationToJlMtHaryono = destinationCustomer.distanceTo(jlMtHaryono) / 1000
        val destinationToJlMayjendSoetojo = destinationCustomer.distanceTo(jlMayjendSoetojo) / 1000
        val destinationToJlSunanGripit = destinationCustomer.distanceTo(jlSunanGripit) / 1000
        val destinationToJlRejasa = destinationCustomer.distanceTo(jlRejasa) / 1000
        val destinationToJlMadukara = destinationCustomer.distanceTo(jlMadukara) / 1000
        val destinationToJlSingamerta = destinationCustomer.distanceTo(jlSingamerta) / 1000
        val destinationToJlLetjendSuprapto =
            destinationCustomer.distanceTo(jlLetjendSuprapto) / 1000
        val destinationToJlAlMunawwaroh = destinationCustomer.distanceTo(jlAlMunawwaroh) / 1000
        val destinationToJlGotongRoyong = destinationCustomer.distanceTo(jlGotongRoyong) / 1000

        // check if the destination location is between two locations
        return destinationToJlVeteran in 0F.rangeTo(0.43942437F) && destinationToJlLetnanKarjono in 0F.rangeTo(
            0.43942437F
        ) || destinationToJlLetnanKarjono in 0F.rangeTo(0.1373421F) && destinationToJlStadion in 0F.rangeTo(
            0.1373421F
        ) || destinationToJlStadion in 0F.rangeTo(0.17309159F) && destinationToJlAjibarangSecang in 0F.rangeTo(
            0.17309159F
        ) || destinationToJlAjibarangSecang in 0F.rangeTo(0.78109133F) && destinationToJlCampurSalam in 0F.rangeTo(
            0.78109133F
        ) || destinationToJlCampurSalam in 0F.rangeTo(0.15971817F) && destinationToJlMtHaryono in 0F.rangeTo(
            0.15971817F
        ) || destinationToJlMtHaryono in 0F.rangeTo(0.25077602F) && destinationToJlMayjendSoetojo in 0F.rangeTo(
            0.25077602F
        ) || destinationToJlMayjendSoetojo in 0F.rangeTo(0.44883206F) && destinationToJlSunanGripit in 0F.rangeTo(
            0.44883206F
        ) || destinationToJlSunanGripit in 0F.rangeTo(0.77481407F) && destinationToJlRejasa in 0F.rangeTo(
            0.77481407F
        ) || destinationToJlRejasa in 0F.rangeTo(6.178497F) && destinationToJlMadukara in 0F.rangeTo(
            6.178497F
        ) || destinationToJlMadukara in 0F.rangeTo(1.3967254F) && destinationToJlSingamerta in 0F.rangeTo(
            1.3967254F
        ) || destinationToJlSingamerta in 0F.rangeTo(6.34115F) && destinationToJlLetjendSuprapto in 0F.rangeTo(
            6.34115F
        ) || destinationToJlLetjendSuprapto in 0F.rangeTo(0.6694348F) && destinationToJlAlMunawwaroh in 0F.rangeTo(
            0.6694348F
        ) || destinationToJlAlMunawwaroh in 0F.rangeTo(0.28578788F) && destinationToJlGotongRoyong in 0F.rangeTo(
            0.28578788F
        ) || destinationToJlGotongRoyong in 0F.rangeTo(0.9904535F) && destinationToJlMayjendSoetojo in 0F.rangeTo(
            0.9904535F
        ) || destinationToJlMayjendSoetojo in 0F.rangeTo(0.1729356F) && destinationToJlVeteran in 0F.rangeTo(
            0.1729356F
        )
    }
}