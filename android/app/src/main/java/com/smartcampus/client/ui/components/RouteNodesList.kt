package com.smartcampus.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartcampus.client.data.model.CampusNodeDto
import com.smartcampus.client.ui.theme.SmartCampusClientTheme

@Composable
fun RouteNodesList(
    routeNodes: List<CampusNodeDto>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = routeNodes,
            key = { node -> node.id }
        ) { node ->
            RouteNodeCard(node = node)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RouteNodesListPreview() {
    val sampleNodes = listOf(
        CampusNodeDto(
            id = 101L,
            buildingId = 1L,
            floorNumber = 1,
            coordX = 10.0,
            coordY = 20.0,
            nodeType = "ENTRANCE",
            isAccessible = true
        ),
        CampusNodeDto(
            id = 102L,
            buildingId = 1L,
            floorNumber = 1,
            coordX = 15.5,
            coordY = 25.0,
            nodeType = "ELEVATOR",
            isAccessible = true
        ),
        CampusNodeDto(
            id = 103L,
            buildingId = 1L,
            floorNumber = 2,
            coordX = 30.0,
            coordY = 40.0,
            nodeType = "CLASSROOM",
            isAccessible = false
        )
    )
    SmartCampusClientTheme {
        RouteNodesList(routeNodes = sampleNodes)
    }
}



