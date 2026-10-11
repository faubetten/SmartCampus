package com.smartcampus.client.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.smartcampus.client.data.model.CampusNodeDto
import com.smartcampus.client.ui.theme.SmartCampusClientTheme

@Composable
fun RouteNodeCard(
    node: CampusNodeDto,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Node ID: ${node.id} • ${node.nodeType}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Floor: ${node.floorNumber}  |  Coords: (${node.coordX}, ${node.coordY})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Wheelchair Accessible: ${if (node.isAccessible) "Yes" else "No"}",
                style = MaterialTheme.typography.bodySmall,
                color = if (node.isAccessible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RouteNodeCardPreview() {
    SmartCampusClientTheme {
        RouteNodeCard(
            node = CampusNodeDto(
                id = 101L,
                buildingId = 1L,
                floorNumber = 2,
                coordX = 12.5,
                coordY = 45.0,
                nodeType = "CLASSROOM",
                isAccessible = true
            )
        )
    }
}


