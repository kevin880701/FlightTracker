package com.lhr.flighttracker.features.map.data

import androidx.compose.ui.geometry.Offset
import com.lhr.flighttracker.R
import com.lhr.flighttracker.features.floorplan.domain.entity.GpsCoordinate
import com.lhr.flighttracker.features.floorplan.domain.entity.MapBounds
import com.lhr.flighttracker.features.floorplan.domain.entity.MapDefinition
import com.lhr.flighttracker.features.floorplan.domain.entity.MapDefinitionBuilder
import com.lhr.flighttracker.features.floorplan.domain.entity.MapImageSource
import com.lhr.flighttracker.features.floorplan.domain.entity.MapMarker
import com.lhr.flighttracker.features.floorplan.domain.entity.MapType
import com.lhr.flighttracker.features.floorplan.domain.entity.MarkerType
import com.lhr.flighttracker.features.floorplan.domain.entity.NodeType
import com.lhr.flighttracker.features.floorplan.domain.entity.PathEdge
import com.lhr.flighttracker.features.floorplan.domain.entity.PathNetwork
import com.lhr.flighttracker.features.floorplan.domain.entity.PathNode
import com.lhr.flighttracker.features.floorplan.domain.entity.PathType
import com.lhr.flighttracker.features.floorplan.domain.utils.ImageSource

/**
 * 機場路徑網絡和標記數據
 *
 * ## 路徑網絡結構說明 ##
 *
 * 本路徑網絡基於機場的實際走廊佈局進行設計，而非直接連接各地標。
 * 主要結構包含：
 * 1.  **地標節點 (ID 1-19)**：與 `markers` 列表一一對應，代表各個服務設施的實際位置。
 * 2.  **走廊節點 (ID 100+)**：
 * - **上層水平走廊 (Y=350)**：節點 ID 為 101-108。
 * - **下層水平走廊 (Y=851)**：節點 ID 為 111-119。
 * - **垂直走廊 (X=1243)**：節點 ID 為 201-202，這兩個節點同時也是與水平走廊的交匯點。
 *
 * 導航邏輯：各地標節點會先連接至最近的走廊節點，然後沿著走廊網絡進行移動。
 */
object StaticMapData {

    /**
     * 機場內所有可供查詢的地標 (MapMarker) 列表。
     */
    val markers = listOf(
        MapMarker(
            id = 1,
            name = "A1登機門",
            coordinates = Offset(125f, 210f),
            imageSource = ImageSource.FromResource(R.drawable.ic_door),
            type = MarkerType.STANDARD_MARKER, 
            description = "國內線登機門",
            area = "第一航廈1樓",
            operatingHours = "05:00-23:00",
            isAccessible = true,
            searchKeywords = listOf("登機", "gate", "A1", "國內線"),
            category = "交通設施"
        ),
        MapMarker(
            id = 2,
            name = "星巴克",
            coordinates = Offset(430f, 210f),
            imageSource = ImageSource.FromResource(R.drawable.ic_restaurant),
            type = MarkerType.STANDARD_MARKER,
            description = "咖啡廳，提供各式咖啡飲品",
            area = "第一航廈1樓",
            operatingHours = "06:00-22:00",
            phoneNumber = "02-1234-5678",
            isAccessible = true,
            searchKeywords = listOf("咖啡", "星巴克", "starbucks", "飲料"),
            category = "餐飲服務"
        ),
        MapMarker(
            id = 3,
            name = "洗手間",
            coordinates = Offset(820f, 210f),
            imageSource = ImageSource.FromResource(R.drawable.ic_wc),
            type = MarkerType.STANDARD_MARKER,
            description = "公共洗手間，包含無障礙設施",
            area = "第一航廈1樓",
            isAccessible = true,
            searchKeywords = listOf("廁所", "洗手間", "restroom", "toilet"),
            category = "公共設施"
        ),
        MapMarker(
            id = 4,
            name = "服務台",
            coordinates = Offset(1200f, 210f),
            imageSource = ImageSource.FromResource(R.drawable.ic_info),
            type = MarkerType.STANDARD_MARKER,
            description = "旅客服務中心，提供諮詢服務",
            area = "第一航廈1樓",
            operatingHours = "24小時",
            phoneNumber = "02-8888-8888",
            isAccessible = true,
            searchKeywords = listOf("服務台", "information", "客服", "諮詢"),
            category = "資訊服務"
        ),
        MapMarker(
            id = 5,
            name = "免稅店",
            coordinates = Offset(1550f, 210f),
            imageSource = ImageSource.FromResource(R.drawable.ic_shopping),
            type = MarkerType.STANDARD_MARKER,
            description = "免稅商店，販售香水、酒類等商品",
            area = "第一航廈1樓",
            operatingHours = "06:00-23:00",
            isAccessible = true,
            searchKeywords = listOf("免稅", "購物", "duty free", "商店"),
            category = "購物服務"
        ),
        MapMarker(
            id = 6,
            name = "安檢站",
            coordinates = Offset(1770f, 210f),
            imageSource = ImageSource.FromResource(R.drawable.ic_security),
            type = MarkerType.STANDARD_MARKER,
            description = "出境旅客安全檢查站",
            area = "第一航廈1樓",
            operatingHours = "05:00-23:30",
            isAccessible = true,
            searchKeywords = listOf("安檢", "security", "檢查"),
            category = "安全設施"
        ),
        MapMarker(
            id = 7,
            name = "行李領取區",
            coordinates = Offset(2000f, 210f),
            imageSource = ImageSource.FromResource(R.drawable.ic_business),
            type = MarkerType.STANDARD_MARKER,
            description = "入境旅客行李領取轉盤",
            area = "第一航廈1樓",
            isAccessible = true,
            searchKeywords = listOf("行李", "baggage", "領取", "轉盤"),
            category = "行李服務"
        ),
        MapMarker(
            id = 8,
            name = "貴賓休息室",
            coordinates = Offset(2200f, 210f),
            imageSource = ImageSource.FromResource(R.drawable.ic_chair),
            type = MarkerType.STANDARD_MARKER,
            description = "商務艙與會員貴賓休息室",
            area = "第一航廈1樓",
            operatingHours = "05:30-23:00",
            isAccessible = true,
            searchKeywords = listOf("貴賓", "休息室", "lounge", "VIP"),
            category = "特殊服務"
        ),
        MapMarker(
            id = 9,
            name = "電梯",
            coordinates = Offset(250f, 670f),
            imageSource = ImageSource.FromResource(R.drawable.ic_elevator),
            type = MarkerType.STANDARD_MARKER,
            description = "連接各樓層的電梯",
            area = "第一航廈",
            isAccessible = true,
            searchKeywords = listOf("電梯", "elevator", "樓層"),
            category = "交通設施"
        ),
        MapMarker(
            id = 10,
            name = "ATM提款機",
            coordinates = Offset(800f, 670f),
            imageSource = ImageSource.FromResource(R.drawable.ic_atm),
            type = MarkerType.STANDARD_MARKER,
            description = "24小時自動提款機",
            area = "第一航廈1樓",
            isAccessible = true,
            searchKeywords = listOf("ATM", "提款", "現金", "銀行"),
            category = "金融服務"
        ),
        MapMarker(
            id = 11,
            name = "中央大廳",
            coordinates = Offset(1660f, 670f),
            imageSource = ImageSource.FromResource(R.drawable.ic_info),
            type = MarkerType.STANDARD_MARKER,
            description = "航廈中央大廳，交通與服務的樞紐",
            area = "第一航廈1樓",
            isAccessible = true,
            searchKeywords = listOf("大廳", "中央", "lobby", "中心"),
            category = "公共區域"
        ),
        MapMarker(
            id = 12,
            name = "主入口",
            coordinates = Offset(2100f, 670f),
            imageSource = ImageSource.FromResource(R.drawable.ic_door),
            type = MarkerType.STANDARD_MARKER,
            description = "航廈主要出入口",
            area = "第一航廈1樓",
            isAccessible = true,
            searchKeywords = listOf("入口", "entrance", "進入", "大門"),
            category = "交通設施"
        ),
        MapMarker(
            id = 13,
            name = "充電站",
            coordinates = Offset(250f, 1120f),
            imageSource = ImageSource.FromResource(R.drawable.ic_charging_station),
            type = MarkerType.STANDARD_MARKER,
            description = "免費手機與電子設備充電區",
            area = "第一航廈1樓",
            isAccessible = true,
            searchKeywords = listOf("充電", "charging", "手機"),
            category = "電子服務"
        ),
        MapMarker(
            id = 14,
            name = "WiFi 熱點",
            coordinates = Offset(700f, 1120f),
            imageSource = ImageSource.FromResource(R.drawable.ic_wifi),
            type = MarkerType.STANDARD_MARKER,
            description = "機場免費無線網路熱點",
            area = "第一航廈1樓",
            isAccessible = true,
            searchKeywords = listOf("wifi", "網路", "熱點", "internet"),
            category = "電子服務"
        ),
        MapMarker(
            id = 15,
            name = "海關",
            coordinates = Offset(965f, 1120f),
            imageSource = ImageSource.FromResource(R.drawable.ic_customs),
            type = MarkerType.STANDARD_MARKER,
            description = "證照查驗與海關申報處",
            area = "第一航廈1樓",
            isAccessible = true,
            searchKeywords = listOf("海關", "customs", "查驗", "申報"),
            category = "政府服務"
        ),
        MapMarker(
            id = 16,
            name = "計程車招呼站",
            coordinates = Offset(1540f, 1120f),
            imageSource = ImageSource.FromResource(R.drawable.ic_taxi),
            type = MarkerType.STANDARD_MARKER,
            description = "前往市區的計程車乘車處",
            area = "第一航廈1樓",
            isAccessible = true,
            searchKeywords = listOf("計程車", "taxi", "排班", "交通"),
            category = "地面交通"
        ),
        MapMarker(
            id = 17,
            name = "巴士站",
            coordinates = Offset(1800f, 1120f),
            imageSource = ImageSource.FromResource(R.drawable.ic_bus),
            type = MarkerType.STANDARD_MARKER,
            description = "機場巴士與客運乘車處",
            area = "第一航廈1樓",
            isAccessible = true,
            searchKeywords = listOf("巴士", "bus", "客運", "交通"),
            category = "地面交通"
        ),
        MapMarker(
            id = 18,
            name = "機場捷運",
            coordinates = Offset(2140f, 1120f),
            imageSource = ImageSource.FromResource(R.drawable.ic_train),
            type = MarkerType.STANDARD_MARKER,
            description = "前往市區的捷運站入口",
            area = "第一航廈B1",
            isAccessible = true,
            searchKeywords = listOf("捷運", "train", "地鐵", "交通"),
            category = "軌道交通"
        ),
        MapMarker(
            id = 19,
            name = "A2 登機門",
            coordinates = Offset(1250f, 1160f),
            imageSource = ImageSource.FromResource(R.drawable.ic_door),
            type = MarkerType.STANDARD_MARKER,
            description = "國內線登機門",
            area = "第一航廈1樓",
            isAccessible = true,
            searchKeywords = listOf("登機", "gate", "A2", "國內線"),
            category = "交通設施"
        )
    )

    // 定義所有路徑節點 (包含地標節點和走廊節點)
    private val allNodes = listOf(
        // --- 1. 地標節點 (ID 1-19)，座標與 markers 相同 ---
        *markers.map { PathNode(id = it.id, coordinates = it.coordinates) }.toTypedArray(),

        // --- 2. 走廊節點 (ID 100+) ---
        // 上層水平走廊 (Y = 350)
        PathNode(101, Offset(125f, 350f)),
        PathNode(102, Offset(430f, 350f)),
        PathNode(103, Offset(820f, 350f)),
        PathNode(104, Offset(1200f, 350f)),
        PathNode(105, Offset(1550f, 350f)),
        PathNode(106, Offset(1770f, 350f)),
        PathNode(107, Offset(2000f, 350f)),
        PathNode(108, Offset(2200f, 350f)),

        // 下層水平走廊 (Y = 851)
        PathNode(111, Offset(250f, 851f)),
        PathNode(112, Offset(800f, 851f)),
        PathNode(113, Offset(965f, 851f)),
        PathNode(114, Offset(1250f, 851f)),
        PathNode(115, Offset(1540f, 851f)),
        PathNode(116, Offset(1660f, 851f)),
        PathNode(117, Offset(1800f, 851f)),
        PathNode(118, Offset(2100f, 851f)),
        PathNode(119, Offset(2140f, 851f)),

        // 垂直走廊與交匯點 (X = 1243)
        PathNode(201, Offset(1243f, 350f), NodeType.INTERSECTION), // 上層交匯口
        PathNode(202, Offset(1243f, 851f), NodeType.INTERSECTION)  // 下層交匯口
    )

    // 定義節點之間的連接關係 (使用 Pair 表示)
    private val connections = listOf(
        // --- 1. 連接地標到最近的走廊節點 ---
        1 to 101, 2 to 102, 3 to 103, 4 to 104, 5 to 105, 6 to 106, 7 to 107, 8 to 108, // 上層地標
        9 to 111, 10 to 112, 11 to 116, 12 to 118, // 中層地標
        13 to 111, 14 to 112, 15 to 113, 16 to 115, 17 to 117, 18 to 119, 19 to 114, // 下層地標

        // --- 2. 連接走廊節點 ---
        // 上層水平走廊 (Y=350)
        101 to 102, 102 to 103, 103 to 104, 104 to 201, // 左半段 -> 交匯口
        201 to 105, 105 to 106, 106 to 107, 107 to 108, // 交匯口 -> 右半段

        // 下層水平走廊 (Y=851)
        111 to 112, 112 to 113, 113 to 202, // 左半段 -> 交匯口
        202 to 114, 114 to 115, 115 to 116, 116 to 117, 117 to 118, 118 to 119, // 交匯口 -> 右半段

        // 垂直走廊 (X=1243)
        201 to 202
    )

    /**
     * 根據上述節點和連接關係，自動生成路徑網絡
     */
    val pathNetwork = PathNetwork(
        nodes = allNodes,
        edges = connections.flatMap { (fromId, toId) ->
            val fromNode = allNodes.find { it.id == fromId }!!
            val toNode = allNodes.find { it.id == toId }!!
            // 為每條連線建立雙向的路徑邊 (Edge)
            listOf(
                PathEdge.create(id = (fromId * 1000) + toId, fromNode = fromNode, toNode = toNode),
                PathEdge.create(id = (toId * 1000) + fromId, fromNode = toNode, toNode = fromNode)
            )
        }
    )
}

fun createMapDefinitionFromStaticData(): MapDefinition {
    // 定義地圖邊界 - 您需要根據實際地圖調整
    val mapBounds = MapBounds(
        northEast = GpsCoordinate(25.0808, 121.2312),
        southWest = GpsCoordinate(25.0788, 121.2292),
        mapWidth = 1920f,
        mapHeight = 1080f
    )

    // 使用建造者模式建立地圖定義
    return MapDefinitionBuilder()
        .id("airport_terminal_1_1f")
        .name("第一航廈一樓")
        .description("桃園國際機場第一航廈一樓平面圖")
        .imageSource(MapImageSource.FromAsset("1F.png"))
        .mapBounds(mapBounds)
        .pathNetwork(StaticMapData.pathNetwork)
        .markers(StaticMapData.markers)
        .level("1F")
        .mapType(MapType.INDOOR)
        .build()
}