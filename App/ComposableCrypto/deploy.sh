#!/bin/bash

dotnet.exe publish -c Release
rsync --delete -ave ssh bin/Release/net6.0/publish/wwwroot/ root@julius-hardt.de:/var/www/html/ComposableCrypto
