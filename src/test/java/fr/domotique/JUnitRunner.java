package fr.domotique;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
fr.domotique.module.thermostat.thermostatgestion.hysteresis.derive.ChaudiereStatsTest.class,
fr.domotique.module.thermostat.AjoutTempExtTest.class
})

public class JUnitRunner {
  // the class remains empty,
  // used only as a holder for the above annotations
}