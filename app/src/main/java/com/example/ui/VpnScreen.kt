package com.example.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.components.CockpitSpeedCard
import com.example.ui.components.AppBlacklistDialog
import com.example.ui.components.BlacklistSelectorCard
import com.example.ui.components.DeveloperBrandCard
import com.example.ui.components.FooterSection
import com.example.ui.components.HeaderSection
import com.example.ui.components.MetricsRowCards
import com.example.ui.theme.CyberBg

@Composable
fun VpnScreen(
    viewModel: VpnViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vpnStatus by viewModel.vpnStatus.collectAsState()
    val metrics by viewModel.metrics.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val showBlacklistDialog by viewModel.showBlacklistDialog.collectAsState()
    val excludedApplications by viewModel.excludedApplications.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val vpnLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.onVpnPermissionGranted()
    }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CyberBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF06152D), Color(0xFF020919))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeaderSection()
                Spacer(modifier = Modifier.height(12.dp))
                CockpitSpeedCard(
                    vpnStatus = vpnStatus,
                    downloadSpeed = metrics.downloadSpeedKBps,
                    uploadSpeed = metrics.uploadSpeedKBps,
                    onPowerClick = {
                        viewModel.toggleConnection { intent: Intent ->
                            vpnLauncher.launch(intent)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                MetricsRowCards(
                    pingMs = metrics.pingMs,
                    ipAddress = metrics.ipAddress,
                    country = metrics.country,
                    countryCode = metrics.countryCode,
                    onCopyIp = { viewModel.copyIpAddress(metrics.ipAddress) }
                )
                Spacer(modifier = Modifier.height(14.dp))
                BlacklistSelectorCard(
                    selectedCount = excludedApplications.size,
                    onClick = { viewModel.setBlacklistDialogVisible(true) }
                )
                Spacer(modifier = Modifier.height(10.dp))
                DeveloperBrandCard(
                    onTelegramClick = { viewModel.openTelegramChannel(context) }
                )
                Spacer(modifier = Modifier.height(10.dp))
                FooterSection()
                Spacer(modifier = Modifier.height(18.dp))
            }
            if (showBlacklistDialog) {
                AppBlacklistDialog(
                    apps = viewModel.installedApplications,
                    selectedPackages = excludedApplications,
                    onSave = { viewModel.saveExcludedApplications(it) },
                    onDismiss = { viewModel.setBlacklistDialogVisible(false) }
                )
            }
        }
    }
}
