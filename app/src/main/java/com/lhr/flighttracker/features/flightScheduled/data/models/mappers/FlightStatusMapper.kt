package com.lhr.flighttracker.features.flightScheduled.data.models.mappers

import com.lhr.flighttracker.features.flightScheduled.data.models.response.AirlineInfo
import com.lhr.flighttracker.features.flightScheduled.data.models.response.AirportInfo
import com.lhr.flighttracker.features.flightScheduled.data.models.response.RawArrivalFlight
import com.lhr.flighttracker.features.flightScheduled.domain.entity.FlightStatus
import com.lhr.flighttracker.features.flightScheduled.domain.entity.LocalizedString

private val noteMap = mapOf(
    1 to "天氣影響", 2 to "流量管制", 3 to "班機調度", 4 to "航機檢修",
    5 to "班機返航", 6 to "轉降松山", 7 to "轉降臺南", 8 to "轉降臺中", 9 to "轉降澎湖",
    10 to "轉降桃園", 11 to "轉降香港", 12 to "延至明日", 13 to "機場關閉", 14 to "延至隔日", 15 to "前日航班"
)

/**
 * 將多個原始資料來源轉換成 domain 層的 FlightStatus 列表
 * @param rawArrivals 原始航班列表
 * @param airportData 原始機場資料
 * @param airlineData 原始航空公司資料
 * @return List<FlightStatus>
 */
fun mapToFlightStatus(
    rawArrivals: List<RawArrivalFlight>,
    airportData: List<AirportInfo>,
    airlineData: List<AirlineInfo>
): List<FlightStatus> {

    val airportInfoMap = airportData.associateBy { it.iata }
    val airlineInfoMap = airlineData.associateBy { it.airlineIATA }

    return rawArrivals.map { rawFlight ->
        val noteKey = rawFlight.arrivalNote?.toIntOrNull()

        val airlineInfo = airlineInfoMap[rawFlight.airlineIATA]
        val airportInfo = airportInfoMap[rawFlight.departureAirportIATA]
        val fallbackAirlineName = rawFlight.airlineIATA ?: "N/A"
        val fallbackAirportName = rawFlight.departureAirportIATA ?: "N/A"

        // 建立 LocalizedString 保存多國語內容
        val airlineName = LocalizedString(
            chinese = airlineInfo?.chineseAlias ?: fallbackAirlineName,
            english = airlineInfo?.englishName ?: fallbackAirlineName,
            japanese = airlineInfo?.japaneseAlias,
            korean = airlineInfo?.koreanAlias
        )

        val airportName = LocalizedString(
            chinese = airportInfo?.chineseName ?: fallbackAirportName,
            english = airportInfo?.englishName ?: fallbackAirportName,
            japanese = airportInfo?.japaneseName,
            korean = airportInfo?.koreanName
        )

        FlightStatus(
            expectTime = rawFlight.scheduledTime ?: "-",
            realTime = rawFlight.actualTime ?: "-",
            airLineName = airlineName,
            airLineCode = rawFlight.airlineIATA ?: "N/A",
            airLineLogo = "https://www.kia.gov.tw/images/ALL-square/${rawFlight.airlineIATA}.png",
            airLineUrl = "https://www.kia.gov.tw/contact.html#${airlineInfo?.chineseAlias}",
            airLineNum = rawFlight.flightNumber ?: "N/A",
            upAirportCode = rawFlight.departureAirportIATA ?: "N/A",
            upAirportName = airportName,
            airPlaneType = rawFlight.airplaneType ?: "N/A",
            airBoardingGate = rawFlight.bay ?: "-",
            airFlyStatus = rawFlight.arrivalStatus?.toIntOrNull() ?: -1,
            airFlyDelayCause = noteMap[noteKey] ?: ""
        )
    }
}