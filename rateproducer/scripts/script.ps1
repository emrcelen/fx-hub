$endpoint = "http://localhost:8181/api/rates"

$pairs = @(
 "EUR/GBP","EUR/TRY","EUR/USD","GBP/EUR","GBP/USD",
 "TRY/EUR","TRY/USD","USD/EUR","USD/GBP","USD/TRY"
)

foreach ($pair in $pairs) {

    $bid = [math]::Round((Get-Random -Minimum 1.0 -Maximum 2.0), 4)
    $ask = [math]::Round($bid + 0.0002, 4)

    $body = @{
        pair = $pair
        bid  = "$bid"
        ask  = "$ask"
    } | ConvertTo-Json

    Write-Host "▶ Sending $pair bid=$bid ask=$ask"

    try {
        Invoke-RestMethod `
          -Uri $endpoint `
          -Method Post `
          -ContentType "application/json" `
          -Body $body | Out-Null
    }
    catch {
        Add-Type -AssemblyName PresentationFramework
        [System.Windows.MessageBox]::Show($_.Exception.Message, "ERROR", 0, 16)
        exit 1
    }
}

Add-Type -AssemblyName PresentationFramework
[System.Windows.MessageBox]::Show("Tüm rate dataları gönderildi", "OK", 0, 64)

Read-Host "Bitirdi. Kapatmak için Enter"

