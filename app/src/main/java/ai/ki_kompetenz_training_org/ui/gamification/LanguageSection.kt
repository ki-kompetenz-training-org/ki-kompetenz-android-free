@Composable
private fun LanguageSection(
    currentLang: String,
    onLanguageChange: (String) -> Unit,
) {
    val options = listOf(
        Triple("system", stringResource(R.string.settings_language_system), "\uD83D\uDCF1"),
        Triple("de", stringResource(R.string.settings_language_de), "\uD83C\uDDE9\uD83C\uDDEA"),
        Triple("en", stringResource(R.string.settings_language_en), "\uD83C\uDDEC7\uD83C\uDDEC7"),
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(stringResource(R.string.settings_language), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            options.forEach { (key, label, emoji) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLanguageChange(key) }
                        .then(if (currentLang == key) Modifier.background(MaterialTheme.colorScheme.primaryContainer) else Modifier)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(emoji)
                    Spacer(Modifier.width(10.dp))
                    Text(label, modifier = Modifier.weight(1f))
                    if (currentLang == key) Text("\u2713", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
