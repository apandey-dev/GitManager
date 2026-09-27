package com.gitmanager.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gitmanager.app.data.model.GitHubRepo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditRepoBottomSheet(
    initialRepo: GitHubRepo? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (name: String, description: String?, isPrivate: Boolean) -> Unit
) {
    var repoName by remember { mutableStateOf(initialRepo?.name ?: "") }
    var description by remember { mutableStateOf(initialRepo?.description ?: "") }
    var isPrivate by remember { mutableStateOf(initialRepo?.isPrivate ?: false) }
    var nameError by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
            )
        },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Title Header
            Text(
                text = if (initialRepo == null) "Create Repository" else "Edit Repository",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (initialRepo == null) "Configure details for your new GitHub repository" else "Update name, description, and visibility settings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Name Input
            MonochromeTextField(
                value = repoName,
                onValueChange = {
                    repoName = it
                    nameError = null
                },
                label = "Repository Name *",
                placeholder = "e.g., modern-android-app",
                isError = nameError != null,
                errorMessage = nameError
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Description Input
            MonochromeTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description (Optional)",
                placeholder = "Short summary of the project",
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Visibility Selector
            Text(
                text = "Visibility",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MonochromeCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isPrivate = false },
                    borderColor = if (!isPrivate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    containerColor = if (!isPrivate) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Public,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Public",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Anyone can see",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                MonochromeCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isPrivate = true },
                    borderColor = if (isPrivate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    containerColor = if (isPrivate) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Private",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Only you can see",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MonochromeOutlinedButton(
                    text = "Cancel",
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f)
                )

                MonochromePrimaryButton(
                    text = if (initialRepo == null) "Create Repository" else "Save Changes",
                    onClick = {
                        if (repoName.isBlank()) {
                            nameError = "Repository name is required"
                        } else {
                            onConfirm(
                                repoName.trim(),
                                description.trim().ifBlank { null },
                                isPrivate
                            )
                        }
                    },
                    modifier = Modifier.weight(1.3f)
                )
            }
        }
    }
}
