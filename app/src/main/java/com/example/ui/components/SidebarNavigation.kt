package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.M_USER
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen

data class NavMenuItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun SidebarContent(
    currentScreen: AppScreen,
    currentUser: M_USER,
    pendingReviewCount: Int,
    pendingApprovalCount: Int,
    readyFinanceCount: Int,
    settlementPendingCount: Int,
    onNavigate: (AppScreen) -> Unit,
    onOpenRoleSelector: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val menuItems = listOf(
        NavMenuItem(AppScreen.DASHBOARD, "1. Dashboard", Icons.Default.Dashboard),
        NavMenuItem(AppScreen.NEW_REQUEST, "2. New Request", Icons.Default.AddCircle),
        NavMenuItem(AppScreen.TRANSACTIONS, "3. My Transactions", Icons.Default.ReceiptLong),
        NavMenuItem(AppScreen.REVIEW, "4. Review (EA)", Icons.Default.RateReview, pendingReviewCount),
        NavMenuItem(AppScreen.APPROVAL, "5. Approval", Icons.Default.Approval, pendingApprovalCount),
        NavMenuItem(AppScreen.FINANCE, "6. Finance", Icons.Default.AccountBalance, readyFinanceCount),
        NavMenuItem(AppScreen.SETTLEMENT, "7. Settlement", Icons.Default.AssignmentReturn, settlementPendingCount),
        NavMenuItem(AppScreen.DOCUMENTS, "8. Documents", Icons.Default.FolderOpen),
        NavMenuItem(AppScreen.REPORTS, "9. Reports", Icons.Default.BarChart),
        NavMenuItem(AppScreen.MASTER_DATA, "10. Master Data", Icons.Default.Storage),
        NavMenuItem(AppScreen.AUDIT_LOG, "11. Audit Log", Icons.Default.History),
        NavMenuItem(AppScreen.SETTINGS, "12. Settings", Icons.Default.Settings)
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(DarkSurface)
            .padding(vertical = 16.dp)
    ) {
        // Corporate Header
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "AEMS PSI",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "PT Prasad Seeds Indonesia",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldLight
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Advance & Expense Management System",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        Divider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = DarkBorder.copy(alpha = 0.5f)
        )

        // Menu Items List
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
        ) {
            menuItems.forEach { item ->
                val isSelected = currentScreen == item.screen
                val bgColor = if (isSelected) EmeraldContainer.copy(alpha = 0.5f) else Color.Transparent
                val contentColor = if (isSelected) EmeraldLight else TextSecondary

                Surface(
                    color = bgColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable {
                            onNavigate(item.screen)
                            onCloseDrawer()
                        }
                        .testTag("nav_menu_${item.screen.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = contentColor,
                            modifier = Modifier.weight(1f)
                        )
                        if (item.badgeCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) EmeraldPrimary else AccentAmber)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${item.badgeCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = DarkBorder.copy(alpha = 0.5f)
        )

        // User profile in footer
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .clickable { onOpenRoleSelector() }
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EmeraldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser.name.take(2).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldLight
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser.name,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = currentUser.role.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentSky,
                        maxLines = 1
                    )
                }
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Ganti Role",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
