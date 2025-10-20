package com.gis.smartfinance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gis.smartfinance.data.model.FinancialTransaction
import com.gis.smartfinance.data.model.TransactionType
import com.gis.smartfinance.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsSheet(
    transaction: FinancialTransaction,
    onDismiss: () -> Unit,
    onSave: (FinancialTransaction) -> Unit,
    onDelete: () -> Unit
) {
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var editedAmount by remember { mutableStateOf(transaction.amount.toString()) }
    var editedDescription by remember { mutableStateOf(transaction.description) }
    var editedType by remember { mutableStateOf(transaction.type) }
    var editedCategory by remember { mutableStateOf(transaction.category) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isEditMode) "Edit Transaction" else "Transaction Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditMode) {
                EditModeContent(
                    amount = editedAmount,
                    onAmountChange = { editedAmount = it },
                    description = editedDescription,
                    onDescriptionChange = { editedDescription = it },
                    type = editedType,
                    onTypeChange = { editedType = it },
                    category = editedCategory,
                    onCategoryChange = { editedCategory = it },
                    onSave = {
                        val amountDouble = editedAmount.toDoubleOrNull()
                        if (amountDouble != null && amountDouble > 0 &&
                            editedDescription.isNotBlank() && editedCategory.isNotBlank()
                        ) {
                            onSave(transaction.copy(
                                amount = amountDouble,
                                description = editedDescription,
                                type = editedType,
                                category = editedCategory,
                                updatedAt = Date()
                            ))
                            onDismiss()
                        }
                    },
                    onCancel = {
                        editedAmount = transaction.amount.toString()
                        editedDescription = transaction.description
                        editedType = transaction.type
                        editedCategory = transaction.category
                        isEditMode = false
                    }
                )
            } else {
                ViewModeContent(
                    transaction = transaction,
                    onEdit = { isEditMode = true },
                    onDelete = { showDeleteDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = AppColors.Error, modifier = Modifier.size(48.dp)) },
            title = { Text("Delete Transaction?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete this transaction. This action cannot be undone.") },
            confirmButton = {
                Button(onClick = { showDeleteDialog = false; onDelete() }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error)) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ViewModeContent(transaction: FinancialTransaction, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.type == TransactionType.EXPENSE) AppColors.ErrorLight else AppColors.SuccessLight
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (transaction.type == TransactionType.EXPENSE) "Expense" else "Income",
                style = MaterialTheme.typography.labelMedium,
                color = if (transaction.type == TransactionType.EXPENSE) AppColors.Error else AppColors.Success
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${if (transaction.type == TransactionType.EXPENSE) "-" else "+"}${String.format("%.2f", transaction.amount)} Lek",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.EXPENSE) AppColors.Error else AppColors.Success
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    DetailRow(Icons.Default.Description, "Description", transaction.description)
    Spacer(modifier = Modifier.height(16.dp))
    DetailRow(Icons.Default.Category, "Category", transaction.category)
    Spacer(modifier = Modifier.height(16.dp))
    DetailRow(Icons.Default.CalendarToday, "Date", formatFullDate(transaction.date))
    Spacer(modifier = Modifier.height(16.dp))
    DetailRow(Icons.Default.AccessTime, "Time", formatTime(transaction.date))

    Spacer(modifier = Modifier.height(32.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onEdit, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Edit, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit", fontWeight = FontWeight.Bold)
        }
        OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Delete, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EditModeContent(
    amount: String, onAmountChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    type: TransactionType, onTypeChange: (TransactionType) -> Unit,
    category: String, onCategoryChange: (String) -> Unit,
    onSave: () -> Unit, onCancel: () -> Unit
) {
    val categories = if (type == TransactionType.EXPENSE)
        listOf("Food & Dining", "Transport", "Shopping", "Entertainment", "Bills", "Healthcare", "Education", "Other")
    else listOf("Salary", "Freelance", "Investment", "Gift", "Other")

    Text("Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilterChip(selected = type == TransactionType.EXPENSE, onClick = { onTypeChange(TransactionType.EXPENSE) }, label = { Text("Expense") }, modifier = Modifier.weight(1f), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AppColors.ErrorLight, selectedLabelColor = AppColors.Error))
        FilterChip(selected = type == TransactionType.INCOME, onClick = { onTypeChange(TransactionType.INCOME) }, label = { Text("Income") }, modifier = Modifier.weight(1f), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AppColors.SuccessLight, selectedLabelColor = AppColors.Success))
    }

    Spacer(modifier = Modifier.height(20.dp))
    Text("Amount", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = amount, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) onAmountChange(it) }, label = { Text("Amount") }, prefix = { Text("Lek ", fontWeight = FontWeight.Bold) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppColors.Purple, focusedLabelColor = AppColors.Purple))

    Spacer(modifier = Modifier.height(20.dp))
    Text("Description", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = description, onValueChange = onDescriptionChange, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppColors.Purple, focusedLabelColor = AppColors.Purple))

    Spacer(modifier = Modifier.height(20.dp))
    Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    Spacer(modifier = Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { cat ->
                    FilterChip(selected = category == cat, onClick = { onCategoryChange(cat) }, label = { Text(cat, fontSize = 12.sp, fontWeight = if (category == cat) FontWeight.Bold else FontWeight.Normal) }, modifier = Modifier.weight(1f), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = if (type == TransactionType.EXPENSE) AppColors.ErrorLight else AppColors.SuccessLight, selectedLabelColor = if (type == TransactionType.EXPENSE) AppColors.Error else AppColors.Success))
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    Spacer(modifier = Modifier.height(32.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("Cancel", fontWeight = FontWeight.Bold) }
        Button(onClick = onSave, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple), shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Default.Check, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = AppColors.Purple, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatFullDate(date: Date) = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(date)
private fun formatTime(date: Date) = SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)