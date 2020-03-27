# Common-Projects

VCF Converter - простая консольная программа, написанная на Java 13, для преобразования содержимого файлов *.vcf (включая множественные визитные карточки в одном файле) в удобно читаемый текстовый формат.

Файл *.vcf можно прочитать в любом текстовом редакторе. Информация в нём интуитивно структурирована и понятна, но формат строк в виде hex-кодов символов UTF-8 (например: =D0=A9=D0=B7...) некорректно читается подавляющим большинством программ обработки виртуальных визиток. У данной программы этого недостатка нет.

VCF Converter имеет 2 функции:
1. Простая конвертация закодированных строк в обычный текст с сохранением структуры *.vcf файла.
2. Преобразование визиток в обычный текстовый файл с выводом данных в читаемой форме (поддерживает не все теги стандарта VCF!).
