package com.maomei.petchatapp.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * アプリ専用ローカルストレージに保存された写真ファイルを表示する。
 * ネットワークやコンテンツプロバイダを経由しない、単純な BitmapFactory 読み込み。
 * 読み込み失敗時（ファイル無し・破損など）はプレースホルダアイコンを表示する。
 */
@Composable
fun LocalPhotoBox(
    path: String?,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = path) {
        value = path?.let { p ->
            withContext(Dispatchers.IO) {
                runCatching { BitmapFactory.decodeFile(p)?.asImageBitmap() }.getOrNull()
            }
        }
    }

    val boxModifier = if (shape != null) modifier.clip(shape) else modifier

    Box(
        modifier = boxModifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 会話画面などで使う丸型のペット写真アイコン。 */
@Composable
fun PetRoundIcon(path: String?, modifier: Modifier = Modifier) {
    LocalPhotoBox(path = path, modifier = modifier, shape = CircleShape)
}

/**
 * 会話画面のペット発言に使う丸型アイコン。
 * 仕様書 5.3「写真読み込み失敗時はメイン写真を使用」に対応するため、
 * 選択された写真の読み込みに失敗した場合はメイン写真にフォールバックする。
 */
@Composable
fun PetMessageIcon(photoPath: String?, mainPhotoPath: String?, modifier: Modifier = Modifier) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = photoPath, key2 = mainPhotoPath) {
        value = withContext(Dispatchers.IO) {
            val primary = photoPath?.let { runCatching { BitmapFactory.decodeFile(it)?.asImageBitmap() }.getOrNull() }
            primary ?: mainPhotoPath?.let { runCatching { BitmapFactory.decodeFile(it)?.asImageBitmap() }.getOrNull() }
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
