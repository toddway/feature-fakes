package com.example

import dagger.Component
import dagger.Provides
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier annotation class Outside
@Qualifier annotation class Inside
interface FakeLocationFeatureFlag { val isEnabled : Boolean }
interface LocationService
class DeviceLocationService @Inject constructor(): LocationService
class KansasCityLocationService @Inject constructor(): LocationService
class DeviceTemperatureSensor @Inject constructor(): TemperatureService {
    override fun get(): Int { TODO("Not yet implemented") }
}

@Component(modules = [
    BigThermostatApp.Module::class,
    FeatureFlagModule::class,
    LocationModule::class,
    TemperatureModule::class
]) @Singleton
interface BigThermostatApp : BasicThermostatApp {
    @dagger.Module
    class Module {
        @Provides fun thermostat(
            @Outside service: TemperatureService,
            @Inside inside: TemperatureService
        ) = Thermostat(service)
    }
}

@dagger.Module
class FeatureFlagModule {
    @Provides @Singleton fun fakeTempFlag() =
        object: FakeTemperatureFeature { override val isEnabled = true }

    @Provides @Singleton fun fakeLocationFlag() =
        object: FakeLocationFeatureFlag { override val isEnabled = false }
}

@dagger.Module
class LocationModule {
    @Provides fun locationService(
        fakeLocation : FakeLocationFeatureFlag,
        real : Provider<DeviceLocationService>,
        fake : Provider<KansasCityLocationService>
    ) = object: LocationService { }
}

@dagger.Module
class TemperatureModule {
    @Provides fun nationalWeatherTempService(locationService: LocationService) = WeatherGovTemperatureService()
    @Provides fun always70TempService() = Always70TemperatureService()
    @Provides @Outside
    fun outsideTemperatureService(
        flags: FakeTemperatureFeature,
        real: Provider<WeatherGovTemperatureService>,
        fake: Provider<Always70TemperatureService>
    ) = if (flags.isEnabled) fake.get() else real.get()
    @Provides @Inside
    fun insideTemperatureService(
        fakeTemps: FakeTemperatureFeature,
        real: Provider<DeviceTemperatureSensor>,
        fake: Provider<Always70TemperatureService>
    ) = if (fakeTemps.isEnabled) fake.get() else real.get()
}

