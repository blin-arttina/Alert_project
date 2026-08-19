package com.assetsalert.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.assetsalert.app.R
import kotlinx.coroutines.launch

private data class OnboardingPage(val title: String, val body: String)

private val pages = listOf(
    OnboardingPage(
        "Never miss a move",
        "Assets Alert watches crypto and stock prices for you and pings the moment your target is hit."
    ),
    OnboardingPage(
        "Impossible to sleep through",
        "When a target hits, an escalating alarm ramps up over time — starting quiet, ending loud, just like the doc promised."
    ),
    OnboardingPage(
        "Make it yours",
        "Pick your own alert sound and stay in dark mode day or night. Let's get your first alert set up."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(140.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                    Text(
                        pages[page].title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        pages[page].body,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { i ->
                    val active = pagerState.currentPage == i
                    Box(
                        Modifier
                            .padding(4.dp)
                            .size(if (active) 10.dp else 8.dp)
                            .then(Modifier)
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(16.dp)) {
                if (pagerState.currentPage < pages.lastIndex) {
                    TextButton(onClick = onFinished, modifier = Modifier.weight(1f)) { Text("Skip") }
                    Button(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        modifier = Modifier.weight(1f)
                    ) { Text("Next") }
                } else {
                    Button(onClick = onFinished, modifier = Modifier.fillMaxWidth()) { Text("Get started") }
                }
            }
        }
    }
}
