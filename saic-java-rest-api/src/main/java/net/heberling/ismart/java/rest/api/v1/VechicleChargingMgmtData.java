package net.heberling.ismart.java.rest.api.v1;

/**
 * @author Doug Culnane
 */
public class VechicleChargingMgmtData extends JsonResponseMessage {

  ViechicleChargingMgmtDataData data;

  public ViechicleChargingMgmtDataData getData() {
    return data;
  }

  public void setData(ViechicleChargingMgmtDataData data) {
    this.data = data;
  }

  public class ViechicleChargingMgmtDataData {
    public RvsChargeStatus getRvsChargeStatus() {
      return rvsChargeStatus;
    }

    public void setRvsChargeStatus(RvsChargeStatus rvsChargeStatus) {
      this.rvsChargeStatus = rvsChargeStatus;
    }

    public ChrgMgmtData getChrgMgmtData() {
      return chrgMgmtData;
    }

    public void setChrgMgmtData(ChrgMgmtData chrgMgmtData) {
      this.chrgMgmtData = chrgMgmtData;
    }

    RvsChargeStatus rvsChargeStatus;
    ChrgMgmtData chrgMgmtData;
  }

  public class RvsChargeStatus {

    Integer mileageSinceLastCharge;
    Integer totalBatteryCapacity;
    Integer workingVoltage;
    Integer chargingDuration;
    Integer chargingType;
    Integer lastChargeEndingPower;

    /** Value / 10 = Range */
    Integer fuelRangeElec;

    Integer realtimePower;
    Integer workingCurrent;

    /** Gun connected 0 = Disconnected 1 = Connected */
    Integer chargingGunState;

    Integer mileageOfDay;
    Long startTime;
    Long endTime;
    Integer powerUsageOfDay;
    Integer powerUsageSinceLastCharge;

    /** Odometer */
    Integer mileage;

    public Integer getMileageSinceLastCharge() {
      return mileageSinceLastCharge;
    }

    public void setMileageSinceLastCharge(Integer mileageSinceLastCharge) {
      this.mileageSinceLastCharge = mileageSinceLastCharge;
    }

    public Integer getTotalBatteryCapacity() {
      return totalBatteryCapacity;
    }

    public void setTotalBatteryCapacity(Integer totalBatteryCapacity) {
      this.totalBatteryCapacity = totalBatteryCapacity;
    }

    public Integer getWorkingVoltage() {
      return workingVoltage;
    }

    public void setWorkingVoltage(Integer workingVoltage) {
      this.workingVoltage = workingVoltage;
    }

    public Integer getChargingDuration() {
      return chargingDuration;
    }

    public void setChargingDuration(Integer chargingDuration) {
      this.chargingDuration = chargingDuration;
    }

    public Integer getChargingType() {
      return chargingType;
    }

    public void setChargingType(Integer chargingType) {
      this.chargingType = chargingType;
    }

    public Integer getLastChargeEndingPower() {
      return lastChargeEndingPower;
    }

    public void setLastChargeEndingPower(Integer lastChargeEndingPower) {
      this.lastChargeEndingPower = lastChargeEndingPower;
    }

    public Integer getFuelRangeElec() {
      return fuelRangeElec;
    }

    public void setFuelRangeElec(Integer fuelRangeElec) {
      this.fuelRangeElec = fuelRangeElec;
    }

    public Integer getRealtimePower() {
      return realtimePower;
    }

    public void setRealtimePower(Integer realtimePower) {
      this.realtimePower = realtimePower;
    }

    public Integer getWorkingCurrent() {
      return workingCurrent;
    }

    public void setWorkingCurrent(Integer workingCurrent) {
      this.workingCurrent = workingCurrent;
    }

    public Integer getChargingGunState() {
      return chargingGunState;
    }

    public void setChargingGunState(Integer chargingGunState) {
      this.chargingGunState = chargingGunState;
    }

    public Integer getMileageOfDay() {
      return mileageOfDay;
    }

    public void setMileageOfDay(Integer mileageOfDay) {
      this.mileageOfDay = mileageOfDay;
    }

    public Long getStartTime() {
      return startTime;
    }

    public void setStartTime(Long startTime) {
      this.startTime = startTime;
    }

    public Long getEndTime() {
      return endTime;
    }

    public void setEndTime(Long endTime) {
      this.endTime = endTime;
    }

    public Integer getPowerUsageOfDay() {
      return powerUsageOfDay;
    }

    public void setPowerUsageOfDay(Integer powerUsageOfDay) {
      this.powerUsageOfDay = powerUsageOfDay;
    }

    public Integer getPowerUsageSinceLastCharge() {
      return powerUsageSinceLastCharge;
    }

    public void setPowerUsageSinceLastCharge(Integer powerUsageSinceLastCharge) {
      this.powerUsageSinceLastCharge = powerUsageSinceLastCharge;
    }

    public Integer getMileage() {
      return mileage;
    }

    public void setMileage(Integer mileage) {
      this.mileage = mileage;
    }
  }

  public class ChrgMgmtData {

    Integer clstrElecRngToEPT;

    /** BMS Charging Status 0 = Not charging. 1 = Charging 5 = Waiting for Charger */
    Integer bmsChrgSts;

    Integer bmsPackSOCDsp;
    Integer bmsPTCHeatReqDspCmd;
    Integer bmsPackCrnt;
    Integer bmsChrgCtrlDspCmd;
    Integer bmsReserStMintueDspCmd;
    Integer bmsOnBdChrgTrgtSOCDspCmd;
    Integer bmsEstdElecRng;
    Integer bmsReserCtrlDspCmd;
    Integer bmsChrgOtptCrntReq;
    Integer bmsPTCHeatSpRsn;
    Integer bmsReserStHourDspCmd;
    Integer bmsPackVol;
    Integer bmsReserSpHourDspCmd;
    Integer bmsAdpPubChrgSttnDspCmd;
    Integer chrgngRmnngTimeV;
    Integer bmsReserSpMintueDspCmd;
    Integer bmsAltngChrgCrntDspCmd;
    Integer bmsChrgSpRsn;

    /** Charging remaining time */
    Integer chrgngRmnngTime;

    public Integer getClstrElecRngToEPT() {
      return clstrElecRngToEPT;
    }

    public void setClstrElecRngToEPT(Integer clstrElecRngToEPT) {
      this.clstrElecRngToEPT = clstrElecRngToEPT;
    }

    public Integer getBmsChrgSts() {
      return bmsChrgSts;
    }

    public void setBmsChrgSts(Integer bmsChrgSts) {
      this.bmsChrgSts = bmsChrgSts;
    }

    public Integer getBmsPackSOCDsp() {
      return bmsPackSOCDsp;
    }

    public void setBmsPackSOCDsp(Integer bmsPackSOCDsp) {
      this.bmsPackSOCDsp = bmsPackSOCDsp;
    }

    public Integer getBmsPTCHeatReqDspCmd() {
      return bmsPTCHeatReqDspCmd;
    }

    public void setBmsPTCHeatReqDspCmd(Integer bmsPTCHeatReqDspCmd) {
      this.bmsPTCHeatReqDspCmd = bmsPTCHeatReqDspCmd;
    }

    public Integer getBmsPackCrnt() {
      return bmsPackCrnt;
    }

    public void setBmsPackCrnt(Integer bmsPackCrnt) {
      this.bmsPackCrnt = bmsPackCrnt;
    }

    public Integer getBmsChrgCtrlDspCmd() {
      return bmsChrgCtrlDspCmd;
    }

    public void setBmsChrgCtrlDspCmd(Integer bmsChrgCtrlDspCmd) {
      this.bmsChrgCtrlDspCmd = bmsChrgCtrlDspCmd;
    }

    public Integer getBmsReserStMintueDspCmd() {
      return bmsReserStMintueDspCmd;
    }

    public void setBmsReserStMintueDspCmd(Integer bmsReserStMintueDspCmd) {
      this.bmsReserStMintueDspCmd = bmsReserStMintueDspCmd;
    }

    public Integer getBmsOnBdChrgTrgtSOCDspCmd() {
      return bmsOnBdChrgTrgtSOCDspCmd;
    }

    public void setBmsOnBdChrgTrgtSOCDspCmd(Integer bmsOnBdChrgTrgtSOCDspCmd) {
      this.bmsOnBdChrgTrgtSOCDspCmd = bmsOnBdChrgTrgtSOCDspCmd;
    }

    public Integer getBmsEstdElecRng() {
      return bmsEstdElecRng;
    }

    public void setBmsEstdElecRng(Integer bmsEstdElecRng) {
      this.bmsEstdElecRng = bmsEstdElecRng;
    }

    public Integer getBmsReserCtrlDspCmd() {
      return bmsReserCtrlDspCmd;
    }

    public void setBmsReserCtrlDspCmd(Integer bmsReserCtrlDspCmd) {
      this.bmsReserCtrlDspCmd = bmsReserCtrlDspCmd;
    }

    public Integer getBmsChrgOtptCrntReq() {
      return bmsChrgOtptCrntReq;
    }

    public void setBmsChrgOtptCrntReq(Integer bmsChrgOtptCrntReq) {
      this.bmsChrgOtptCrntReq = bmsChrgOtptCrntReq;
    }

    public Integer getBmsPTCHeatSpRsn() {
      return bmsPTCHeatSpRsn;
    }

    public void setBmsPTCHeatSpRsn(Integer bmsPTCHeatSpRsn) {
      this.bmsPTCHeatSpRsn = bmsPTCHeatSpRsn;
    }

    public Integer getBmsReserStHourDspCmd() {
      return bmsReserStHourDspCmd;
    }

    public void setBmsReserStHourDspCmd(Integer bmsReserStHourDspCmd) {
      this.bmsReserStHourDspCmd = bmsReserStHourDspCmd;
    }

    public Integer getBmsPackVol() {
      return bmsPackVol;
    }

    public void setBmsPackVol(Integer bmsPackVol) {
      this.bmsPackVol = bmsPackVol;
    }

    public Integer getBmsReserSpHourDspCmd() {
      return bmsReserSpHourDspCmd;
    }

    public void setBmsReserSpHourDspCmd(Integer bmsReserSpHourDspCmd) {
      this.bmsReserSpHourDspCmd = bmsReserSpHourDspCmd;
    }

    public Integer getBmsAdpPubChrgSttnDspCmd() {
      return bmsAdpPubChrgSttnDspCmd;
    }

    public void setBmsAdpPubChrgSttnDspCmd(Integer bmsAdpPubChrgSttnDspCmd) {
      this.bmsAdpPubChrgSttnDspCmd = bmsAdpPubChrgSttnDspCmd;
    }

    public Integer getChrgngRmnngTimeV() {
      return chrgngRmnngTimeV;
    }

    public void setChrgngRmnngTimeV(Integer chrgngRmnngTimeV) {
      this.chrgngRmnngTimeV = chrgngRmnngTimeV;
    }

    public Integer getBmsReserSpMintueDspCmd() {
      return bmsReserSpMintueDspCmd;
    }

    public void setBmsReserSpMintueDspCmd(Integer bmsReserSpMintueDspCmd) {
      this.bmsReserSpMintueDspCmd = bmsReserSpMintueDspCmd;
    }

    public Integer getBmsAltngChrgCrntDspCmd() {
      return bmsAltngChrgCrntDspCmd;
    }

    public void setBmsAltngChrgCrntDspCmd(Integer bmsAltngChrgCrntDspCmd) {
      this.bmsAltngChrgCrntDspCmd = bmsAltngChrgCrntDspCmd;
    }

    public Integer getBmsChrgSpRsn() {
      return bmsChrgSpRsn;
    }

    public void setBmsChrgSpRsn(Integer bmsChrgSpRsn) {
      this.bmsChrgSpRsn = bmsChrgSpRsn;
    }

    public Integer getChrgngRmnngTime() {
      return chrgngRmnngTime;
    }

    public void setChrgngRmnngTime(Integer chrgngRmnngTime) {
      this.chrgngRmnngTime = chrgngRmnngTime;
    }
  }
}
