package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.NoteItem
import com.example.data.Suggestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(viewModel: OmniViewModel, onOpenHistory: () -> Unit) {
    val currentNote by viewModel.currentNote.collectAsStateWithLifecycle()
    val currentItems by viewModel.currentItems.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    
    var itemName by remember { mutableStateOf(TextFieldValue("")) }
    var quantity by remember { mutableStateOf(TextFieldValue("1")) }
    var section by remember { mutableStateOf(TextFieldValue("")) }
    var showSuggestions by remember { mutableStateOf(false) }
    
    var editingItem by remember { mutableStateOf<NoteItem?>(null) }
    var editName by remember { mutableStateOf(TextFieldValue("")) }
    var editQuantity by remember { mutableStateOf(TextFieldValue("")) }

    val context = LocalContext.current

    fun printNote() {
        // Simple HTML print to support Bluetooth printers via Android Print Service
        val html = buildString {
            append("<html><body style='font-family: Arial; direction: rtl;'>")
            append("<h2 style='text-align: center;'>${currentNote?.title ?: ""}</h2>")
            append("<table style='width: 100%; border-collapse: collapse; table-layout: fixed;'>")
            val items = currentItems
            for (i in items.indices step 2) {
                append("<tr>")
                // Left col (order: Action/Return -> Name -> Qty)
                val left = items[i]
                append("<td style='border: 1px solid black; width: 5%; text-align: center;'>X</td>")
                append("<td style='border: 1px solid black; width: 35%; padding: 4px;'>${left.name}</td>")
                append("<td style='border: 1px solid black; width: 10%; text-align: center;'>${if (left.quantity % 1.0 == 0.0) left.quantity.toInt() else left.quantity}</td>")
                
                // Right col
                if (i + 1 < items.size) {
                    val right = items[i+1]
                    append("<td style='border: 1px solid black; width: 35%; padding: 4px;'>${right.name}</td>")
                    append("<td style='border: 1px solid black; width: 10%; text-align: center;'>${if (right.quantity % 1.0 == 0.0) right.quantity.toInt() else right.quantity}</td>")
                } else {
                    append("<td colspan='2' style='border: 1px solid black;'></td>")
                }
                append("</tr>")
            }
            append("</table></body></html>")
        }
        
        val webView = android.webkit.WebView(context)
        webView.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageFinished(view: android.webkit.WebView, url: String) {
                val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
                val printAdapter = view.createPrintDocumentAdapter("Smart Note")
                printManager.print("Smart Note", printAdapter, null)
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    Scaffold(
        topBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.primaryContainer).statusBarsPadding().padding(top = 12.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ActionChip(stringResource(R.string.clear_page), Color(0xFFFF9800)) { viewModel.clearCurrentNote() }
                    ActionChip(stringResource(R.string.new_note), Color(0xFF2196F3)) { viewModel.createNewNote() }
                    ActionChip(stringResource(R.string.history), Color(0xFF9C27B0)) { onOpenHistory() }
                    ActionChip(stringResource(R.string.smart_print), Color(0xFF4CAF50)) { printNote() }
                    ActionChip(stringResource(R.string.print_split), Color(0xFF00BCD4)) { printNote() }
                }
                
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.updateNoteSettings((currentNote?.fontSize ?: 14) - 1, currentNote?.scrollEnabled ?: true) }) {
                            Icon(Icons.Default.Remove, null)
                        }
                        Text("${stringResource(R.string.font_size)} ${currentNote?.fontSize ?: 14}")
                        IconButton(onClick = { viewModel.updateNoteSettings((currentNote?.fontSize ?: 14) + 1, currentNote?.scrollEnabled ?: true) }) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                    
                    Row(
                        Modifier.clickable { viewModel.updateNoteSettings(currentNote?.fontSize ?: 14, !(currentNote?.scrollEnabled ?: true)) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${stringResource(R.string.scroll_mode)} ${if (currentNote?.scrollEnabled == true) stringResource(R.string.on) else stringResource(R.string.off)}")
                        Switch(
                            checked = currentNote?.scrollEnabled ?: true,
                            onCheckedChange = { viewModel.updateNoteSettings(currentNote?.fontSize ?: 14, it) },
                            modifier = Modifier.scale(0.7f)
                        )
                    }
                }
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            // Input Area
            Card(
                Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E))
            ) {
                Column(Modifier.padding(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text(stringResource(R.string.quantity), color = Color.White) },
                            modifier = Modifier.weight(0.3f).onFocusChanged { 
                                if (it.isFocused) quantity = quantity.copy(selection = TextRange(0, quantity.text.length)) 
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                        Box(Modifier.weight(0.7f)) {
                            OutlinedTextField(
                                value = itemName,
                                onValueChange = { 
                                    itemName = it
                                    showSuggestions = it.text.isNotEmpty()
                                },
                                label = { Text(stringResource(R.string.item_name), color = Color.White) },
                                modifier = Modifier.fillMaxWidth().onFocusChanged {
                                    if (it.isFocused) itemName = itemName.copy(selection = TextRange(0, itemName.text.length))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = Color.White,
                                    unfocusedLabelColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                            if (showSuggestions && suggestions.any { it.word.contains(itemName.text, ignoreCase = true) }) {
                                Card(
                                    Modifier.fillMaxWidth().padding(top = 60.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column {
                                        suggestions.filter { it.word.contains(itemName.text, ignoreCase = true) }.take(5).forEach { sug ->
                                            Text(
                                                sug.word,
                                                Modifier.fillMaxWidth().clickable {
                                                    itemName = TextFieldValue(sug.word, TextRange(sug.word.length))
                                                    showSuggestions = false
                                                }.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        OutlinedTextField(
                            value = section,
                            onValueChange = { section = it },
                            label = { Text(stringResource(R.string.target_section), color = Color.White) },
                            modifier = Modifier.weight(0.4f).onFocusChanged {
                                if (it.isFocused) section = section.copy(selection = TextRange(0, section.text.length))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (itemName.text.isNotBlank()) {
                                viewModel.addItem(itemName.text, quantity.text.toDoubleOrNull() ?: 1.0, section.text)
                                itemName = TextFieldValue("")
                                quantity = TextFieldValue("1")
                                section = TextFieldValue("")
                                showSuggestions = false
                            }
                        },
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.save_item))
                    }
                }
            }
            
            // List Area (Two columns like the screenshot)
            val fontSize = (currentNote?.fontSize ?: 14).sp
            val items = currentItems
            val leftItems = items.filterIndexed { index, _ -> index % 2 == 0 }
            val rightItems = items.filterIndexed { index, _ -> index % 2 != 0 }
            val maxRows = maxOf(leftItems.size, rightItems.size)

            LazyColumn(Modifier.fillMaxSize().padding(4.dp)) {
                items(maxRows) { rowIndex ->
                    Row(Modifier.fillMaxWidth()) {
                        // Left Column Item
                        if (rowIndex < leftItems.size) {
                            NoteItemRow(
                                leftItems[rowIndex], 
                                fontSize, 
                                Modifier.weight(1f),
                                onUpdate = { viewModel.updateItem(it) },
                                onDelete = { viewModel.deleteItem(it) }
                            )
                        } else {
                            Box(Modifier.weight(1f))
                        }
                        
                        // Vertical Divider
                        Box(Modifier.width(1.dp).fillMaxHeight().background(Color.Gray))
                        
                        // Right Column Item
                        if (rowIndex < rightItems.size) {
                            NoteItemRow(
                                rightItems[rowIndex], 
                                fontSize, 
                                Modifier.weight(1f),
                                onUpdate = { viewModel.updateItem(it) },
                                onDelete = { viewModel.deleteItem(it) }
                            )
                        } else {
                            Box(Modifier.weight(1f))
                        }
                    }
                    HorizontalDivider(color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun NoteItemRow(
    item: NoteItem, 
    fontSize: androidx.compose.ui.unit.TextUnit, 
    modifier: Modifier, 
    onUpdate: (NoteItem) -> Unit,
    onDelete: (NoteItem) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(TextFieldValue(item.name)) }
    var editQty by remember { mutableStateOf(TextFieldValue(if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString())) }

    if (isEditing) {
        AlertDialog(
            onDismissRequest = { isEditing = false },
            title = { Text(stringResource(R.string.edit_item)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(stringResource(R.string.item_name)) },
                        modifier = Modifier.onFocusChanged {
                            if (it.isFocused) editName = editName.copy(selection = TextRange(0, editName.text.length))
                        }
                    )
                    OutlinedTextField(
                        value = editQty,
                        onValueChange = { editQty = it },
                        label = { Text(stringResource(R.string.quantity)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.onFocusChanged {
                            if (it.isFocused) editQty = editQty.copy(selection = TextRange(0, editQty.text.length))
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdate(item.copy(name = editName.text, quantity = editQty.text.toDoubleOrNull() ?: 1.0))
                    isEditing = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { isEditing = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Row(
        modifier.padding(4.dp).clickable { isEditing = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onDelete(item) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
        }
        Box(Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
        Text(
            text = item.name,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            fontSize = fontSize,
            textAlign = TextAlign.End,
            color = Color(0xFF0D47A1)
        )
        Box(Modifier.width(1.dp).height(24.dp).background(Color.LightGray))
        Text(
            text = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString(),
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.Center,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActionChip(text: String, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = color,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.height(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
