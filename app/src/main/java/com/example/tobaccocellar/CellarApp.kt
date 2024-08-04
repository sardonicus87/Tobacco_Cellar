@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.tobaccocellar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.tobaccocellar.ui.navigation.CellarNavHost
import com.example.tobaccocellar.ui.theme.primaryLight

@Composable
fun CellarApp(navController: NavHostController = rememberNavController()) {
    CellarNavHost(navController = navController)
}

@Composable
fun CellarTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    showMenu: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = primaryLight
        ),
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            if (showMenu) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { R.string.import_csv }, onClick = { expanded = false })
                        DropdownMenuItem(text = { R.string.export_csv }, onClick = { expanded = false })
                        DropdownMenuItem(text = { R.string.settings}, onClick = { expanded = false })
                    }
                }
            }
        }
    )
}

@Composable
fun CellarBottomAppBar(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit = {},
    navigateToStats: () -> Unit = {},
    navigateToAddEntry: () -> Unit = {},
) {
   BottomAppBar(
       modifier = modifier
           .fillMaxWidth()
           .padding(0.dp)
           .height(66.dp),
       contentPadding = PaddingValues(0.dp),
   ) {
       /* TODO add tinting for current page/option */
       Row (
           modifier = Modifier
               .fillMaxSize()
               .padding(0.dp)
               .height(66.dp),
           horizontalArrangement = Arrangement.SpaceBetween,
           verticalAlignment = Alignment.CenterVertically
       ) {

           // Cellar //
           Column (
               modifier = Modifier
                   .weight(1f),
               verticalArrangement = Arrangement.spacedBy(0.dp),
               horizontalAlignment = Alignment.CenterHorizontally,
           ){
               IconButton(
                   onClick = navigateToHome,
                   modifier = Modifier
                       .padding(0.dp)
               ){
                    Icon(
                        painter = painterResource(id = R.drawable.table_view_old),
                        contentDescription = stringResource(R.string.home_title),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(28.dp)
                    )
               }
               Text(
                   text = stringResource(R.string.home_title),
                   fontSize = 12.sp,
                   fontWeight = FontWeight.Normal,
                   modifier = Modifier
                       .offset(y = (-8).dp)
               )
           }

           // Stats //
           Column (
               modifier = Modifier
                   .weight(1f),
               verticalArrangement = Arrangement.spacedBy(0.dp),
               horizontalAlignment = Alignment.CenterHorizontally,
           ) {
               IconButton(
                   onClick = navigateToStats,
                   modifier = Modifier
                       .padding(0.dp)
               ) {
                   Icon(
                       painter = painterResource(id = R.drawable.bar_chart),
                       contentDescription = stringResource(R.string.stats_title),
                       tint = MaterialTheme.colorScheme.onBackground,
                       modifier = Modifier
                           .size(28.dp)
                   )
               }
               Text(
                   text = stringResource(R.string.stats_title),
                   fontSize = 12.sp,
                   fontWeight = FontWeight.Normal,
                   modifier = Modifier
                       .offset(y = (-8).dp)
               )
           }

           // Filter //
           Column (
               modifier = Modifier
                   .weight(1f),
               verticalArrangement = Arrangement.spacedBy(0.dp),
               horizontalAlignment = Alignment.CenterHorizontally,
           ) {
               IconButton(
                   onClick = { /*TODO Filter (Sheets?)*/ },
                   modifier = Modifier
                       .padding(0.dp)
               ) {
                   Icon(
                       painter = painterResource(id = R.drawable.filter),
                       contentDescription = stringResource(R.string.filter_items),
                       tint = MaterialTheme.colorScheme.onBackground,
                       modifier = Modifier
                           .size(28.dp)
                   )
               }
               Text(
                   text = stringResource(R.string.filter_items),
                   fontSize = 12.sp,
                   fontWeight = FontWeight.Normal,
                   modifier = Modifier
                       .offset(y = (-8).dp)
               )
           }

           // Add //
           Column (
               modifier = Modifier
                   .weight(1f),
               verticalArrangement = Arrangement.spacedBy(0.dp),
               horizontalAlignment = Alignment.CenterHorizontally,
           ) {
               IconButton(
                   onClick = navigateToAddEntry,
                   modifier = Modifier
                       .padding(0.dp)
               ) {
                   Icon(
                      painter = painterResource(id = R.drawable.add),
                      contentDescription = stringResource(R.string.add),
                      tint = MaterialTheme.colorScheme.onBackground,
                      modifier = Modifier
                           .size(28.dp)
                   )
               }
               Text(
                   text = stringResource(R.string.add),
                   fontSize = 12.sp,
                   fontWeight = FontWeight.Normal,
                   modifier = Modifier
                       .offset(y = (-8).dp)
               )
           }
       }
   }
}

@Preview
@Composable
fun BottomBarPreview(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit = {},
    navigateToStats: () -> Unit = {},
    showBackground: Boolean = true,
) {
    CellarBottomAppBar {}
}