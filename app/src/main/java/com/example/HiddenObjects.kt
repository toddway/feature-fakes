package com.example

import com.squareup.moshi.Moshi
import dagger.Component
import dagger.Provides
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request


@Component(modules = [BasicThermostatApp.Module::class])
interface BasicThermostatApp {
    fun thermostat() : Thermostat

    @dagger.Module
    class Module {
        @Provides fun thermostat(service : WeatherGovTemperatureService) = Thermostat(service)
        @Provides fun service() = WeatherGovTemperatureService()
    }
}

@Component(modules = [FakeThermostatApp.Module::class])
interface FakeThermostatApp : BasicThermostatApp {
    @dagger.Module
    class Module {
        @Provides fun thermostat(service : Always70TemperatureService) = Thermostat(service)
        @Provides fun service() = Always70TemperatureService()
    }
}

@Component(modules = [ThermostatAppWithFlags.Module::class])
interface ThermostatAppWithFlags : BasicThermostatApp {
    @dagger.Module
    class Module {
        @Provides fun nationalWeatherTempService() = WeatherGovTemperatureService()
        @Provides fun always70TempService() = Always70TemperatureService()
        @Provides fun thermostat(
            fakeTemperatureFeature: FakeTemperatureFeature,
            weatherGovTemperatureService: WeatherGovTemperatureService,
            always70TemperatureService: Always70TemperatureService
        ) = Thermostat(
            if (fakeTemperatureFeature.isEnabled) always70TemperatureService
            else weatherGovTemperatureService
        )
        @Provides fun fakeTemperatureFeature() = object: FakeTemperatureFeature { override val isEnabled = true }
    }
}

class HourlyForecast(private val properties: Properties? = null) {
    class Properties(val periods: List<Period>)
    class Period(val temperature: Int)
    fun currentTemperature() = properties?.periods?.get(0)?.temperature ?: -1

    companion object {
        fun from(url: String) = from(OkHttpClient().newCall(Request.Builder().url(url).build()))
        fun from(call: Call) = call.execute().body?.source()?.let { adapter.fromJson(it) } ?: HourlyForecast()
        private val adapter = Moshi.Builder().build().adapter(HourlyForecast::class.java)
    }
}


