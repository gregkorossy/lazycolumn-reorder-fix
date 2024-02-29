package me.gingerninja.lazylist.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.gingerninja.lazylist.hijacker.rememberLazyGridStateHijacker
import me.gingerninja.lazylist.hijacker.rememberLazyListStateHijacker
import me.gingerninja.lazylist.sample.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var hijackEnabled by remember { mutableStateOf(true) }
            var gridEnabled by remember { mutableStateOf(false) }

            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(text = if (gridEnabled) "LazyGrid hijack" else "LazyColumn hijack")
                                },
                                actions = {
                                    Switch(
                                        checked = hijackEnabled,
                                        onCheckedChange = { checked ->
                                            hijackEnabled = checked

                                            scope.launch {
                                                snackbarHostState.currentSnackbarData?.dismiss()

                                                if (checked) {
                                                    snackbarHostState.showSnackbar("Hijack enabled")
                                                } else {
                                                    snackbarHostState.showSnackbar("Hijack disabled")
                                                }
                                            }
                                        }
                                    )
                                }
                            )
                        },
                        snackbarHost = {
                            SnackbarHost(
                                hostState = snackbarHostState
                            )
                        }
                    ) { padding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                        ) {
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                SegmentedButton(
                                    selected = !gridEnabled,
                                    onClick = { gridEnabled = false },
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                ) {
                                    Text(text = "Column")
                                }
                                SegmentedButton(
                                    selected = gridEnabled,
                                    onClick = { gridEnabled = true },
                                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                ) {
                                    Text(text = "Grid")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (gridEnabled) {
                                TodoGrid(
                                    modifier = Modifier
                                        .weight(1.0f)
                                        .fillMaxSize(),
                                    hijackEnabled = hijackEnabled
                                )
                            } else {
                                TodoList(
                                    modifier = Modifier
                                        .weight(1.0f)
                                        .fillMaxSize(),
                                    hijackEnabled = hijackEnabled
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TodoList(
    modifier: Modifier = Modifier,
    hijackEnabled: Boolean,
) {
    var list by remember {
        mutableStateOf(
            List(50) {
                TodoItem(
                    id = it,
                    name = "Item #$it"
                )
            }
        )
    }

    fun toggleItem(id: Int, checked: Boolean) {
        list = list.toMutableList().apply {
            val index = indexOfFirst { it.id == id }

            val item = removeAt(index).copy(checked = checked)

            if (checked) {
                add(item)
            } else {
                add(0, item)
            }
        }
    }

    val listState = rememberLazyListState()
    rememberLazyListStateHijacker(listState = listState, enabled = hijackEnabled)

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = list, key = { item -> item.id }) {
            TodoItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItemPlacement(),
                name = it.name,
                checked = it.checked,
                onCheckedChange = { checked ->
                    toggleItem(it.id, checked)
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TodoGrid(
    modifier: Modifier = Modifier,
    hijackEnabled: Boolean,
) {
    var list by remember {
        mutableStateOf(
            List(50) {
                TodoItem(
                    id = it,
                    name = "Item #$it"
                )
            }
        )
    }

    fun toggleItem(id: Int, checked: Boolean) {
        list = list.toMutableList().apply {
            val index = indexOfFirst { it.id == id }

            val item = removeAt(index).copy(checked = checked)

            if (checked) {
                add(item)
            } else {
                add(0, item)
            }
        }
    }

    val gridState = rememberLazyGridState()
    rememberLazyGridStateHijacker(gridState = gridState, enabled = hijackEnabled)

    LazyVerticalGrid(
        modifier = modifier,
        state = gridState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        columns = GridCells.Adaptive(120.dp)
    ) {
        items(items = list, key = { item -> item.id }) {
            TodoItem(
                modifier = Modifier
                    .width(120.dp)
                    .animateItemPlacement(),
                name = it.name,
                checked = it.checked,
                onCheckedChange = { checked ->
                    toggleItem(it.id, checked)
                }
            )
        }
    }
}

@Composable
private fun TodoItem(
    name: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )

            Text(
                text = name,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .weight(1f),
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
            )
        }

    }
}

data class TodoItem(
    val id: Int,
    val name: String,
    val checked: Boolean = false
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppTheme {
        TodoList(
            modifier = Modifier.fillMaxSize(),
            hijackEnabled = true
        )
    }
}
