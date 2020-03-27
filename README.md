# VCF

VCF Converter - простая консольная программа, написанная на Java 13, для преобразования содержимого файлов *.vcf (включая множественные визитные карточки в одном файле) в удобно читаемый текстовый формат.

Файл *.vcf можно прочитать в любом текстовом редакторе. Информация в нём интуитивно структурирована и понятна, но формат строк в виде hex-кодов символов UTF-8 (например: =D0=A9=D0=B7...) некорректно читается подавляющим большинством программ обработки виртуальных визиток. У данной программы этого недостатка нет.

VCF Converter имеет 2 функции:
1. Простая конвертация закодированных строк в обычный текст с сохранением структуры *.vcf файла.
2. Преобразование визиток в обычный текстовый файл с выводом данных в читаемой форме (поддерживает не все теги стандарта VCF!).

Как пользоваться:
1. Установить OpenJDK 13 (прописать в переменной PATH и JAVA_HOME).
2. Настроить и запустить файл VCF.bat, который лежит рядом с VCF.jar

HOWTO: start java -jar VCF.jar {params}
<br>params:
<br> -I:'pathname.vcf'{;'pathname.vcf'} = set input files
<br> -F1 = decode UTF-8 strings in input files
<br> -F2 = create txt data files in user-friendly format
<br>Example: start java -jar VCF.jar -I:"/vcfs/contacts1.vcf";"/vcfs/contacts2.vcf";"/vcfs/contacts3.vcf" -F1 -F2

Пример:

Допустим, есть некоторый файл contacts.vcf, созданный на телефоне с Android. Его содержимое имеет вид:

BEGIN:VCARD
<br>VERSION:2.1
<br>N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=D0=9A=D0=BE=D0=BD=D0=BD=D0=B8=D0=BA;=D0=94=D0=BC=B8=D1;;;
<br>FN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:=D0=94=D0=BC=B8=D1=20=D0=9A=D0=BE=D0=BD=D0=BD=
<br>=D0=B8=D0=BA
<br>TEL;CELL:+7 123 456-78-90
<br>ORG:GITHUB
<br>END:VCARD
<br>BEGIN:VCARD

Тогда преобразование по ключу -F1 создаст файл contacts.vcf_F1.txt, который будет иметь вид:

BEGIN:VCARD
<br>VERSION:2.1
<br>N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:Иванов;Иван;;;
<br>FN;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:Иван Иванов
<br>TEL;CELL:+7 123 456-78-90
<br>ORG:GITHUB
<br>END:VCARD
<br>BEGIN:VCARD

Преобразовние по ключу -F2 создаст файл contacts.vcf_F2.txt, который будет иметь вид:

<br>===========================================================================
<br>     ФИО: Иванов Иван
<br> Телефон: +7(123)456-78-90
<br>  Работа: GITHUB
<br>===========================================================================
