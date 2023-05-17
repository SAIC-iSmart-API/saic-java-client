package net.heberling.ismart.mqtt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VehicleStateTest {

  public static final int REFERENCE_TIME = 1684273208;
  @Mock private MqttClient mqttClient;

  private Clock clock;
  private VehicleState vehicleState;

  @BeforeEach
  public void setUp() throws MqttException {
    clock = Clock.fixed(Instant.ofEpochSecond(REFERENCE_TIME), ZoneId.systemDefault());
    vehicleState = new VehicleState(mqttClient, "test", () -> this.clock);
    vehicleState.notifyCarActivityTime(ZonedDateTime.now(clock), true);
  }

  @Test
  public void willAllwaysRefreshOnFirstCall() {
    assertThat(vehicleState.shouldRefresh(), is(true));
  }

  @Test
  public void willRefreshIfCarWasActiveAfterLastRefresh() throws MqttException {
    clock = Clock.offset(clock, java.time.Duration.ofSeconds(60));
    vehicleState.notifyCarActivityTime(ZonedDateTime.now(clock), true);
    assertThat(vehicleState.shouldRefresh(), is(true));
  }

  @Test
  public void willNotRefreshIfApiReturns10SubsequentErrors() throws MqttException {
    clock = Clock.offset(clock, java.time.Duration.ofSeconds(60));
    vehicleState.markSuccessfulRefresh();
    for (int i = 0; i < 10; i++) {
      vehicleState.apiUpdateError();
    }
    assertThat(vehicleState.shouldRefresh(), is(false));
  }

  @Test
  public void willNeverRefreshIfRefreshModeIsOff() throws MqttException {
    clock = Clock.offset(clock, java.time.Duration.ofSeconds(60));
    vehicleState.setRefreshMode(RefreshMode.OFF);
    assertThat(vehicleState.shouldRefresh(), is(false));
  }

  @Test
  public void willRefreshIfRefreshModeIsForce() {
    vehicleState.setRefreshMode(RefreshMode.FORCE);
    assertThat(vehicleState.shouldRefresh(), is(true));
    assertThat(vehicleState.getRefreshMode(), is(RefreshMode.PERIODIC));
  }

  @Test
  public void willNotRefreshIfActiveBefore30s() throws MqttException {
    vehicleState.markSuccessfulRefresh();
    clock = Clock.offset(clock, java.time.Duration.ofSeconds(29));
    assertThat(vehicleState.shouldRefresh(), is(false));
  }

  @Test
  public void willRefreshIfActiveAfter30s() throws MqttException {
    vehicleState.markSuccessfulRefresh();
    clock = Clock.offset(clock, java.time.Duration.ofSeconds(31));
    assertThat(vehicleState.shouldRefresh(), is(true));
  }

  @Test
  public void willNotRefreshIfInactiveBefore1Day() throws MqttException {
    vehicleState.markSuccessfulRefresh();
    vehicleState.setHVBatteryActive(false);
    clock = Clock.offset(clock, java.time.Duration.ofDays(1).minusSeconds(1));
    assertThat(vehicleState.shouldRefresh(), is(false));
  }

  @Test
  public void willRefreshIfInactiveAfter1Day() throws MqttException {
    vehicleState.markSuccessfulRefresh();
    vehicleState.setHVBatteryActive(false);
    clock = Clock.offset(clock, java.time.Duration.ofDays(1).plusSeconds(1));
    assertThat(vehicleState.shouldRefresh(), is(true));
  }
}
