package net.heberling.ismart.mqtt;

import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.convert.ChainedFactory;
import com.owlike.genson.ext.javadatetime.JavaDateTimeBundle;
import com.owlike.genson.reflect.TypeUtil;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.heberling.ismart.Client;
import net.heberling.ismart.asn1.AbstractMessage;
import net.heberling.ismart.asn1.AbstractMessageCoder;
import net.heberling.ismart.asn1.Anonymizer;
import net.heberling.ismart.asn1.v1_1.Message;
import net.heberling.ismart.asn1.v1_1.MessageCoder;
import net.heberling.ismart.asn1.v1_1.entity.AlarmSwitch;
import net.heberling.ismart.asn1.v1_1.entity.AlarmSwitchReq;
import net.heberling.ismart.asn1.v1_1.entity.MP_AlarmSettingType;
import net.heberling.ismart.asn1.v1_1.entity.MP_UserLoggingInReq;
import net.heberling.ismart.asn1.v1_1.entity.MP_UserLoggingInResp;
import net.heberling.ismart.cli.UTF8StringObjectWriter;
import org.bn.annotations.ASN1Enum;
import org.bn.annotations.ASN1Sequence;
import org.bn.coders.IASN1PreparedElement;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
    name = "ismart-mqtt-gateway",
    mixinStandardHelpOptions = true,
    versionProvider = SaicMqttGateway.VersionProvider.class)
public class SaicMqttGateway implements Callable<Integer> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SaicMqttGateway.class);

  static class ConfigFileConverter implements CommandLine.ITypeConverter<File> {

    @Override
    public File convert(String value) throws Exception {
      var file = new File(value);
      if (file.exists()) {
        final var tomlMapper = new TomlMapper();
        final var data = tomlMapper.readValue(file, Map.class);
        final var data2 = flat("config", data);
        data2.forEach((k, v) -> System.getProperties().putIfAbsent(k, v));
      } else {
        throw new FileNotFoundException(value);
      }
      return file;
    }
  }

  @CommandLine.Option(
      order = Integer.MIN_VALUE,
      converter = ConfigFileConverter.class,
      names = {"-c", "--config"},
      description = {
        "The config file to use. Options can be overridden by cli parameters and environment"
            + " variables"
      })
  private File config;

  @CommandLine.Option(
      names = {"-m", "--mqtt-uri"},
      required = true,
      description = {"The URI to the MQTT Server.", "Environment Variable: MQTT_URI"},
      defaultValue = "${env:MQTT_URI:-${config.mqtt.uri}}")
  private URI mqttUri;

  @CommandLine.Option(
      names = {"--mqtt-user"},
      description = {"The MQTT user name.", "Environment Variable: MQTT_USER"},
      defaultValue = "${env:MQTT_USER:-${config.mqtt.username}}")
  private String mqttUser;

  @CommandLine.Option(
      names = {"--mqtt-password"},
      description = {"The MQTT password.", "Environment Variable: MQTT_PASSWORD"},
      defaultValue = "${env:MQTT_PASSWORD:-${config.mqtt.password}}")
  private char[] mqttPassword;

  @CommandLine.Option(
      names = {"-s", "--saic-uri"},
      description = {
        "The SAIC uri.",
        "Environment Variable: SAIC_URI",
        "Default is the European Production Endpoint: https://tap-eu.soimt.com"
      },
      defaultValue = "${env:SAIC_URI:-${config.saic.uri:-https://tap-eu.soimt.com}}")
  private URI saicUri;

  @CommandLine.Option(
      names = {"-u", "--saic-user"},
      required = true,
      description = {"The SAIC user name.", "Environment Variable: SAIC_USER"},
      defaultValue = "${env:SAIC_USER:-${config.saic.username}}")
  private String saicUser;

  @CommandLine.Option(
      names = {"-p", "--saic-password"},
      required = true,
      description = {"The SAIC password.", "Environment Variable: SAIC_PASSWORD"},
      defaultValue = "${env:SAIC_PASSWORD:-${config.saic.password}}")
  private String saicPassword;

  @CommandLine.Option(
      names = {"--abrp-api-key"},
      description = {
        "The API key for the A Better Route Planer telemetry API.",
        "Default is the open source telemetry API key 8cfc314b-03cd-4efe-ab7d-4431cd8f2e2d",
        "Environment Variable: ABRP_API_KEY"
      },
      defaultValue =
          "${env:ABRP_API_KEY:-${sys:config.abrp.api-key:-8cfc314b-03cd-4efe-ab7d-4431cd8f2e2d}}")
  private String abrpApiKey;

  @CommandLine.Option(
      names = {"--abrp-user-token"},
      description = {
        "The mapping of VIN to ABRP User Token.",
        "Multiple mappings can be provided seperated by ,",
        "Example: LSJXXXX=12345-abcdef,LSJYYYY=67890-ghijkl",
        "Environment Variable: ABRP_USER_TOKEN"
      },
      defaultValue = "${env:ABRP_USER_TOKEN:-${sys:config.abrp.token:-_NULL_}}",
      split = ",")
  private Map<String, String> vinAbrpTokenMap = new HashMap<>();

  @CommandLine.Option(
          names = {"-i", "--polling-interval"},
          required = false,
          description = {"Polling Interval in seconds between SAIC API requests"},
          defaultValue = "${env:POLLING_INTERVAL:-${config.polling.interval}}")
  private long pollingInterval = 0;

  private IMqttClient client;

  private final Map<String, VehicleHandler> vehicleHandlerMap = new HashMap<>();

  @Override
  public Integer call() throws Exception { // your business logic goes here...
    String publisherId = UUID.randomUUID().toString();
    try (IMqttClient client =
        new MqttClient(mqttUri.toString(), publisherId, null) {
          @Override
          public void close() throws MqttException {
            disconnect();
            super.close(true);
          }
        }) {
      this.client = client;
      MqttConnectOptions options = new MqttConnectOptions();
      options.setAutomaticReconnect(true);
      options.setCleanSession(true);
      options.setConnectionTimeout(10);
      if (mqttUser != null) {
        options.setUserName(mqttUser);
      }
      if (mqttPassword != null) {
        options.setPassword(mqttPassword);
      }
      client.connect(options);

      client.setCallback(
          new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {}

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
              LOGGER.debug("Got message for topic {}: {}", topic, message);
              final Matcher matcher =
                  Pattern.compile(".*/vehicles/([^/]*)/(.*)/set").matcher(topic);
              if (matcher.matches()) {
                new Thread(
                        () -> {
                          try {
                            vehicleHandlerMap
                                .get(matcher.group(1))
                                .handleMQTTCommand(matcher.group(2), message);
                          } catch (MqttException e) {
                            LOGGER.error(
                                "Could not handle message for topic {}: {}", topic, message, e);
                          }
                        })
                    .start();
              }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
          });

      var mqttAccountPrefix = "saic/" + saicUser;

      client.subscribe(mqttAccountPrefix + "/vehicles/+/+/+/set");

      MessageCoder<MP_UserLoggingInReq> loginRequestMessageCoder =
          new MessageCoder<>(MP_UserLoggingInReq.class);

      MP_UserLoggingInReq applicationData = new MP_UserLoggingInReq();
      applicationData.setPassword(saicPassword);
      Message<MP_UserLoggingInReq> loginRequestMessage =
          loginRequestMessageCoder.initializeMessage(
              "0000000000000000000000000000000000000000000000000#".substring(saicUser.length())
                  + saicUser,
              null,
              null,
              "501",
              513,
              1,
              applicationData);

      String loginRequest = loginRequestMessageCoder.encodeRequest(loginRequestMessage);

      LOGGER.debug(toJSON(anonymized(loginRequestMessageCoder, loginRequestMessage)));

      String loginResponse = Client.sendRequest(saicUri.resolve("/TAP.Web/ota.mp"), loginRequest);

      Message<MP_UserLoggingInResp> loginResponseMessage =
          new MessageCoder<>(MP_UserLoggingInResp.class).decodeResponse(loginResponse);

      // register for all known alarm types (not all might be actually delivered)
      for (MP_AlarmSettingType.EnumType type : MP_AlarmSettingType.EnumType.values()) {
        registerAlarmMessage(
            loginResponseMessage.getBody().getUid(),
            loginResponseMessage.getApplicationData().getToken(),
            type);
      }

      LOGGER.debug(
          toJSON(anonymized(new MessageCoder<>(MP_UserLoggingInResp.class), loginResponseMessage)));
      List<Future<?>> futures =
          loginResponseMessage.getApplicationData().getVinList().stream()
              .map(
                  vin -> {
                    VehicleHandler handler =
                        new VehicleHandler(
                            this,
                            client,
                            saicUri,
                            loginResponseMessage.getBody().getUid(),
                            loginResponseMessage.getApplicationData().getToken(),
                            mqttAccountPrefix,
                            vin,
                            pollingInterval);
                    vehicleHandlerMap.put(vin.getVin(), handler);
                    return handler;
                  })
              .map(
                  handler ->
                      (Callable<Object>)
                          () -> {
                            handler.handleVehicle();
                            return null;
                          })
              .map(Executors.newSingleThreadExecutor()::submit)
              .collect(Collectors.toList());

      ScheduledFuture<?> pollingJob =
          createMessagePoller(
              loginResponseMessage.getBody().getUid(),
              loginResponseMessage.getApplicationData().getToken(),
              mqttAccountPrefix);

      futures.add(pollingJob);

      for (Future<?> future : futures) {
        // make sure we wait on all futures before exiting
        future.get();
      }
      return 0;
    }
  }

  private void registerAlarmMessage(String uid, String token, MP_AlarmSettingType.EnumType type)
      throws NoSuchAlgorithmException, IOException {
    MessageCoder<AlarmSwitchReq> alarmSwitchReqMessageCoder =
        new MessageCoder<>(AlarmSwitchReq.class);

    AlarmSwitchReq alarmSwitchReq = new AlarmSwitchReq();
    alarmSwitchReq.setAlarmSwitchList(
        Stream.of(type).map(v -> createAlarmSwitch(v, true)).collect(Collectors.toList()));
    alarmSwitchReq.setPin(hashMD5("123456"));

    Message<AlarmSwitchReq> alarmSwitchMessage =
        alarmSwitchReqMessageCoder.initializeMessage(
            uid, token, null, "521", 513, 1, alarmSwitchReq);
    String alarmSwitchRequest = alarmSwitchReqMessageCoder.encodeRequest(alarmSwitchMessage);
    String alarmSwitchResponse =
        Client.sendRequest(saicUri.resolve("/TAP.Web/ota.mp"), alarmSwitchRequest);
    final MessageCoder<IASN1PreparedElement> alarmSwitchResMessageCoder =
        new MessageCoder<>(IASN1PreparedElement.class);
    Message<IASN1PreparedElement> alarmSwitchResponseMessage =
        alarmSwitchResMessageCoder.decodeResponse(alarmSwitchResponse);

    LOGGER.debug(toJSON(anonymized(alarmSwitchResMessageCoder, alarmSwitchResponseMessage)));

    if (alarmSwitchResponseMessage.getBody().getErrorMessage() != null) {
      LOGGER.warn(
          "Could not register for {} messages: {}",
          type,
          new String(
              alarmSwitchResponseMessage.getBody().getErrorMessage(), StandardCharsets.UTF_8));
    } else {
      LOGGER.info("Registered for {} messages", type);
    }
  }

  private static AlarmSwitch createAlarmSwitch(MP_AlarmSettingType.EnumType type, boolean enabled) {
    AlarmSwitch alarmSwitch = new AlarmSwitch();
    MP_AlarmSettingType alarmSettingType = new MP_AlarmSettingType();
    alarmSettingType.setValue(type);
    alarmSettingType.setIntegerForm(type.ordinal());
    alarmSwitch.setAlarmSettingType(alarmSettingType);
    alarmSwitch.setAlarmSwitch(enabled);
    alarmSwitch.setFunctionSwitch(enabled);
    return alarmSwitch;
  }

  public String hashMD5(String password) throws NoSuchAlgorithmException {

    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(password.getBytes());
    byte[] digest = md.digest();
    return AbstractMessageCoder.bytesToHex(digest);
  }

  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  private ScheduledFuture<?> createMessagePoller(
      String uid, String token, String mqttAccountPrefix) {
    return Executors.newSingleThreadScheduledExecutor()
        .scheduleWithFixedDelay(
            new MessageHandler(saicUri, uid, token, mqttAccountPrefix, this),
            1,
            1,
            TimeUnit.SECONDS);
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new SaicMqttGateway()).execute(args);
    System.exit(exitCode);
  }

  private static Map<String, String> flat(String prefix, Map<String, Object> nested) {
    Map<String, String> map = new HashMap<>();
    nested.forEach(
        (key, value) -> {
          var newPrefix = prefix + "." + key;
          if (value instanceof Map) {
            map.putAll(flat(newPrefix, (Map<String, Object>) value));
          } else if (value instanceof Collection) {
            map.put(
                newPrefix,
                ((Collection<Object>) value)
                    .stream()
                        .map(o -> (Map<String, String>) o)
                        .flatMap(m -> m.entrySet().stream())
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining(",")));
          } else {
            map.put(newPrefix, value.toString());
          }
        });
    return map;
  }

  static <
          H extends IASN1PreparedElement,
          B extends IASN1PreparedElement,
          E extends IASN1PreparedElement,
          M extends AbstractMessage<H, B, E>>
      M anonymized(AbstractMessageCoder<H, B, E, M> coder, M message) {
    M messageCopy = coder.decodeResponse(coder.encodeRequest(message));
    Anonymizer.anonymize(messageCopy);
    return messageCopy;
  }

  public static void fillReserved(byte[] reservedBytes) {
    System.arraycopy(
        (ThreadLocalRandom.current().nextLong() + "1111111111111111").getBytes(),
        0,
        reservedBytes,
        0,
        16);
  }

  public static String toJSON(Object message) {
    // TODO: make sure this corresponds to the JER ASN.1 serialisation format
    final ChainedFactory chain =
        new ChainedFactory() {
          @Override
          protected Converter<?> create(Type type, Genson genson, Converter<?> nextConverter) {
            return new Converter<>() {
              @Override
              public void serialize(Object object, ObjectWriter writer, Context ctx)
                  throws Exception {
                if (object != null) {
                  writer.beginNextObjectMetadata();
                  if (object.getClass().isAnnotationPresent(ASN1Enum.class)) {
                    writer.writeMetadata(
                        "ASN1Type", object.getClass().getAnnotation(ASN1Enum.class).name());
                  } else if (object.getClass().isAnnotationPresent(ASN1Sequence.class)) {
                    writer.writeMetadata(
                        "ASN1Type", object.getClass().getAnnotation(ASN1Sequence.class).name());
                  }
                }

                @SuppressWarnings("unchecked")
                Converter<Object> n = (Converter<Object>) nextConverter;
                if (!(writer instanceof UTF8StringObjectWriter)) {
                  writer = new UTF8StringObjectWriter(writer);
                }
                n.serialize(object, writer, ctx);
              }

              @Override
              public Object deserialize(ObjectReader reader, Context ctx) throws Exception {
                return nextConverter.deserialize(reader, ctx);
              }
            };
          }
        };
    chain.withNext(
        new ChainedFactory() {
          @Override
          protected Converter<?> create(Type type, Genson genson, Converter<?> converter) {
            final Class<?> clazz = TypeUtil.getRawClass(type);
            if (clazz.isAnnotationPresent(ASN1Enum.class)) {

              return new Converter<>() {
                @Override
                public void serialize(Object o, ObjectWriter objectWriter, Context context)
                    throws Exception {
                  Method getValue = clazz.getMethod("getValue");
                  Object value = getValue.invoke(o);
                  if (value == null) {
                    objectWriter.writeNull();
                  } else {
                    objectWriter.writeString(String.valueOf(value));
                  }
                }

                @Override
                public Object deserialize(ObjectReader objectReader, Context context) {
                  throw new UnsupportedOperationException("not implemented yet");
                }
              };
            } else {

              return converter;
            }
          }
        });
    return new GensonBuilder()
        .useDateAsTimestamp(false)
        .withBundle(new JavaDateTimeBundle())
        .useIndentation(true)
        .useRuntimeType(true)
        .exclude("preparedData")
        .withConverterFactory(chain)
        .create()
        .serialize(message);
  }

  public String getAbrpApiKey() {
    return abrpApiKey;
  }

  public String getAbrpUserToken(String vin) {
    return vinAbrpTokenMap.get(vin);
  }

  public void notifyMessage(String mqttMessagePrefix, SaicMessage message) throws MqttException {
    MqttMessage msg =
        new MqttMessage(SaicMqttGateway.toJSON(message).getBytes(StandardCharsets.UTF_8));
    msg.setQos(0);
    // Don't retain, so deleted messages are removed
    // automatically from the broker
    msg.setRetained(false);
    client.publish(mqttMessagePrefix + "/" + message.getMessageId(), msg);

    if (message.getVin() != null) {
      vehicleHandlerMap.get(message.getVin()).notifyMessage(message);
    }
  }

  static class VersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() {
      return new String[] {getClass().getPackage().getImplementationVersion()};
    }
  }
}
