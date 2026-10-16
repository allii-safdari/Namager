package com.example.data

import com.example.R
import com.example.model.*

object CinemaSampleData {

    val standardVideoQualities = listOf(
        VideoQuality(
            id = "4k",
            label = "4K Ultra HD",
            resolution = "3840×2160",
            fileSize = "۴.۸ گیگابایت",
            bitrate = "18 Mbps",
            description = "بالاترین وضوح تصویر، صدای 5.1 کاناله و رنگ‌های زنده HDR"
        ),
        VideoQuality(
            id = "1080p",
            label = "1080p Full HD",
            resolution = "1920×1080",
            fileSize = "۲.۱ گیگابایت",
            bitrate = "8 Mbps",
            description = "کیفیت فوق‌العاده شفاف، مناسب تلویزیون و تبلت"
        ),
        VideoQuality(
            id = "720p",
            label = "720p HD",
            resolution = "1280×720",
            fileSize = "۱.۲ گیگابایت",
            bitrate = "4 Mbps",
            description = "کیفیت استاندارد با مصرف متعادل اینترنت"
        ),
        VideoQuality(
            id = "480p",
            label = "480p SD",
            resolution = "854×480",
            fileSize = "۶۵۰ مگابایت",
            bitrate = "1.5 Mbps",
            description = "حجم فشرده و کم‌مصرف، مناسب دیتای همراه"
        )
    )

    val genres = listOf(
        Genre("all", "همه", "All"),
        Genre("drama", "درام و اجتماعی", "Drama"),
        Genre("crime", "جنایی و معمایی", "Crime"),
        Genre("classic", "سینمای کلاسیک", "Classic"),
        Genre("action", "اکشن و هیجان‌انگیز", "Action"),
        Genre("romance", "عاشقانه", "Romance"),
        Genre("comedy", "کمدی", "Comedy")
    )

    val movies = listOf(
        MovieItem(
            id = "salesman",
            title = "فروشنده",
            englishTitle = "The Salesman",
            mediaType = MediaType.MOVIE,
            director = "اصغر فرهادی",
            releaseYear = "۱۳۹۵",
            duration = "۱۲۵ دقیقه",
            imdbRating = 7.7f,
            ageRating = "+۱۵",
            backdropRes = R.drawable.img_hero_cinema_1792095559768,
            genres = listOf("درام و اجتماعی", "معمایی"),
            synopsis = "رعنا و عماد زن و شوهر جوانی هستند که در حال اجرای نمایش «مرگ فروشنده» آرتور میلر می‌باشند. به علت نشست زمین در آپارتمانشان، مجبور به اسباب‌کشی به خانه‌ای جدید می‌شوند که مستاجر قبلی آن زنی بدنام بوده است. حادثه‌ای ناگهانی در این خانه، آرامش زندگی آن‌ها را به چالش می‌کشد.",
            awards = listOf("برنده اسکار بهترین فیلم خارجی‌زبان ۲۰۱۷", "برنده نخل طلای بهترین فیلمنامه و بازیگر مرد کن"),
            cast = listOf(
                CastMember("شهاب حسینی", "عماد"),
                CastMember("ترانه علیدوستی", "رعنا"),
                CastMember("بابک کریمی", "بابک"),
                CastMember("فرید سجادی حسینی", "مرد پیر"),
                CastMember("مینا ساداتی", "صنم")
            ),
            isFeatured = true,
            isCriticChoice = true,
            isTrending = true
        ),
        MovieItem(
            id = "lion_skin",
            title = "پوست شیر",
            englishTitle = "The Lion's Skin",
            mediaType = MediaType.SERIES,
            director = "جمشید محمودی",
            releaseYear = "۱۴۰۱",
            duration = "۳ فصل (۲۴ قسمت)",
            imdbRating = 8.4f,
            ageRating = "+۱۸",
            backdropRes = R.drawable.img_series_crime_1792095573776,
            genres = listOf("جنایی و معمایی", "درام و اجتماعی"),
            synopsis = "نعیم پس از گذراندن پانزده سال حبس به جرم سرقت مسلحانه آزاد می‌شود. او تنها یک آرزو دارد: دیدن دخترش ساحل. اما در جریان سفری کوتاه به شمال کشور، دخترش به شکلی هولناک ربوده می‌شود و سرگرد مشکات وارد این پرونده پیچیده جنایی می‌شود.",
            awards = listOf("بهترین سریال نمایش خانگی در جشن حافظ", "پربیننده‌ترین سریال سال در ایران"),
            cast = listOf(
                CastMember("هادی حجازی‌فر", "نعیم مولایی"),
                CastMember("شهاب حسینی", "سرگرد محب مشکات"),
                CastMember("پانته‌آ بهرام", "لیلا برزگر"),
                CastMember("مهرداد صدیقیان", "صدرا کوهستانی"),
                CastMember("پردیس احمدیه", "ساحل مولایی"),
                CastMember("علیرضا کمالی", "رضا پروانه")
            ),
            episodes = listOf(
                Episode(1, 1, "قسمت ۱: رهایی در تاریکی", "۵۲ دقیقه", "نعیم پس از ۱۵ سال از زندان آزاد می‌شود و با کمک دوست قدیمی‌اش رضا به دنبال دخترش می‌گردد.", R.drawable.img_series_crime_1792095573776),
                Episode(1, 2, "قسمت ۲: سایه ساحل", "۴۸ دقیقه", "نعیم و ساحل به سفر می‌روند، اما توقف کوتاهی در جاده سرنوشت آن‌ها را دگرگون می‌کند.", R.drawable.img_series_crime_1792095573776),
                Episode(1, 3, "قسمت ۳: پرونده محب", "۵۵ دقیقه", "سرگرد مشکات که در گذشته ضربه روحی سختی خورده، مسئول پیگیری پرونده مفقودی ساحل می‌شود.", R.drawable.img_series_crime_1792095573776),
                Episode(1, 4, "قسمت ۴: ردپای خونین", "۵۱ دقیقه", "سرنخ‌های اولیه در اطراف ویلا و پیدا شدن وسایل مشکوک، تحقیقات را وارد فاز جدیدی می‌کند.", R.drawable.img_series_crime_1792095573776),
                Episode(1, 5, "قسمت ۵: راز صندوق عقب", "۵۴ دقیقه", "نعیم شخصاً برای پیدا کردن مظنونان وارد عمل می‌شود و با خطرناک‌ترین افراد روبرو می‌شود.", R.drawable.img_series_crime_1792095573776)
            ),
            isFeatured = true,
            isCriticChoice = true,
            isTrending = true
        ),
        MovieItem(
            id = "taste_of_cherry",
            title = "طعم گیلاس",
            englishTitle = "Taste of Cherry",
            mediaType = MediaType.MOVIE,
            director = "عباس کیارستمی",
            releaseYear = "۱۳۷۶",
            duration = "۹۵ دقیقه",
            imdbRating = 7.8f,
            ageRating = "+۱۲",
            backdropRes = R.drawable.img_classic_cinema_1792095588359,
            genres = listOf("سینمای کلاسیک", "درام و اجتماعی"),
            synopsis = "آقای بدیعی مردی میانسال است که قصد خودکشی دارد و با خودروی خود در حومه شهر تهران به دنبال کسی می‌گردد که پس از مرگش، روی مزارش خاک بریزد. در این مسیر او با یک سرباز جوان، یک طلبه و یک موزه دار ترک روبرو می‌شود که نگاه او به زندگی را تغییر می‌دهند.",
            awards = listOf("برنده نخل طلای جشنواره کن ۱۹۹۷", "جزو ۱۰۰ فیلم برتر قرن بیستم به انتخاب مجله سایت اند ساوند"),
            cast = listOf(
                CastMember("همایون ارشادی", "آقای بدیعی"),
                CastMember("عبدالحسین باقری", "آقای باقری"),
                CastMember("افشین خورشید باختری", "سرباز"),
                CastMember("صفرعلی مرادی", "نگهبان")
            ),
            isFeatured = false,
            isCriticChoice = true,
            isTrending = false
        ),
        MovieItem(
            id = "separation",
            title = "جدایی نادر از سیمین",
            englishTitle = "A Separation",
            mediaType = MediaType.MOVIE,
            director = "اصغر فرهادی",
            releaseYear = "۱۳۸۹",
            duration = "۱۲۳ دقیقه",
            imdbRating = 8.3f,
            ageRating = "+۱۳",
            backdropRes = R.drawable.img_hero_cinema_1792095559768,
            genres = listOf("درام و اجتماعی"),
            synopsis = "سیمین می‌خواهد به همراه همسرش نادر و دخترش ترمه از ایران برود. اما نادر به خاطر نگهداری از پدر مبتلا به آلزایمرش موافقت نمی‌کند. جدایی آنها باعث ورود زن پرستاری به نام راضیه و همسر معترضش حجت به زندگی آنها و بروز بحرانی بزرگ می‌شود.",
            awards = listOf("برنده اسکار بهترین فیلم خارجی ۲۰۱۲", "برنده خرس طلای بهترین فیلم جشنواره برلین", "برنده گلدن گلوب"),
            cast = listOf(
                CastMember("پیمان معادی", "نادر"),
                CastMember("لیلا حاتمی", "سیمین"),
                CastMember("شهاب حسینی", "حجت"),
                CastMember("ساره بیات", "راضیه"),
                CastMember("مریلا زارعی", "خانم قریشی")
            ),
            isFeatured = true,
            isCriticChoice = true,
            isTrending = true
        ),
        MovieItem(
            id = "just_6_5",
            title = "متری شیش و نیم",
            englishTitle = "Just 6.5",
            mediaType = MediaType.MOVIE,
            director = "سعید روستایی",
            releaseYear = "۱۳۹۷",
            duration = "۱۳۱ دقیقه",
            imdbRating = 7.9f,
            ageRating = "+۱۵",
            backdropRes = R.drawable.img_series_crime_1792095573776,
            genres = listOf("جنایی و معمایی", "اکشن و هیجان‌انگیز"),
            synopsis = "صمد، رئیس پلیس مبارزه با مواد مخدر، در پی دستگیری یکی از سرشناس‌ترین فروشندگان شیشه پایتخت به نام ناصر خاکزاد است. تقابل این دو شخصیت در جریان بازجویی‌ها و کشف ابعاد پنهان توزیع مواد، داستانی نفس‌گیر می‌سازد.",
            awards = listOf("برنده سیمرغ بهترین فیلم از نگاه تماشاگران جشنواره فجر", "برنده جایزه بهترین کارگردانی و بازیگری جشنواره ونیز افق‌ها"),
            cast = listOf(
                CastMember("پیمان معادی", "صمد مجیدی"),
                CastMember("نوید محمدزاده", "ناصر خاکزاد"),
                CastMember("پریناز ایزدیار", "الهام"),
                CastMember("فرهاد اصلانی", "قاضی ربیعی")
            ),
            isFeatured = false,
            isCriticChoice = true,
            isTrending = true
        ),
        MovieItem(
            id = "frog_series",
            title = "قورباغه",
            englishTitle = "The Frog",
            mediaType = MediaType.SERIES,
            director = "هومن سیدی",
            releaseYear = "۱۳۹۹",
            duration = "۱ فصل (۱۵ قسمت)",
            imdbRating = 8.1f,
            ageRating = "+۱۸",
            backdropRes = R.drawable.img_series_crime_1792095573776,
            genres = listOf("جنایی و معمایی", "اکشن و هیجان‌انگیز"),
            synopsis = "در روز چهارشنبه‌سوری، سه دوست به سرقت از خانه نوری، همکلاسی قدیمی پولدارشان دست می‌زنند. اما نوری رازی ماورایی و مرموز با خود به همراه دارد که سرنوشت آنها را به حمام خون و قدرتی عجیب پیوند می‌زند.",
            awards = listOf("نوآورانه‌ترین سریال از نظر جلوه‌های بصری و تدوین در جشنواره‌های داخلی"),
            cast = listOf(
                CastMember("صابر ابر", "رامین فیاضی"),
                CastMember("نوید محمدزاده", "نوری"),
                CastMember("سحر دولتشاهی", "فرانک بهرامی"),
                CastMember("فرشته حسینی", "لیلا"),
                CastMember("هومن سیدی", "شمس الواعظین")
            ),
            episodes = listOf(
                Episode(1, 1, "قسمت ۱: آفریقا", "۵۴ دقیقه", "نقشه سرقت در شب چهارشنبه‌سوری و ورود به خانه مرموز نوری.", R.drawable.img_series_crime_1792095573776),
                Episode(1, 2, "قسمت ۲: سگ‌های ولگرد", "۴۹ دقیقه", "رامین متوجه می‌شود که زنده مانده و اثر ماده سمی نوری را بررسی می‌کند.", R.drawable.img_series_crime_1792095573776),
                Episode(1, 3, "قسمت ۳: بوی خون تازه", "۵۱ دقیقه", "روایت بازگشت نوری از خارج و کشف گیاه ناشناخته در آمریکای جنوبی.", R.drawable.img_series_crime_1792095573776)
            ),
            isFeatured = false,
            isCriticChoice = false,
            isTrending = true
        ),
        MovieItem(
            id = "mortal_wound",
            title = "زخم کاری",
            englishTitle = "Mortal Wound",
            mediaType = MediaType.SERIES,
            director = "محمدحسین مهدویان",
            releaseYear = "۱۴۰۰",
            duration = "۳ فصل (۳۶ قسمت)",
            imdbRating = 7.6f,
            ageRating = "+۱۵",
            backdropRes = R.drawable.img_hero_cinema_1792095559768,
            genres = listOf("درام و اجتماعی", "جنایی و معمایی"),
            synopsis = "مالکی، از مدیران هولدینگ بزرگ ریزآبادی، با تحریک همسرش سمیرا تصمیم می‌گیرد با از میان برداشتن حاج‌عمو ریزآبادی به ثروت و قدرت کل مجموعه دست پیدا کند؛ اقتباسی مدرن و بومی از مکبث شکسپیر.",
            awards = listOf("برنده جایزه بهترین سریال در بیست و یکمین جشن حافظ"),
            cast = listOf(
                CastMember("جواد عزتی", "مالک مالکی"),
                CastMember("رعنا آزادی‌ور", "سمیرا"),
                CastMember("سیاوش طهمورث", "خان عمو"),
                CastMember("هانیه توسلی", "منصوره ریزآبادی"),
                CastMember("سعید چنگیزیان", "ناصر ریزآبادی")
            ),
            episodes = listOf(
                Episode(1, 1, "قسمت ۱: وسوسه", "۵۰ دقیقه", "مالک قراردادی بزرگ در کیش منعقد می‌کند و نقشه مرگبار را با سمیرا می‌چیند.", R.drawable.img_hero_cinema_1792095559768),
                Episode(1, 2, "قسمت ۲: کلبه شمالی", "۵۳ دقیقه", "سفر به ویلای کلاردشت و اجرای نقشه شوم در وان حمام.", R.drawable.img_hero_cinema_1792095559768)
            ),
            isFeatured = false,
            isCriticChoice = false,
            isTrending = true
        ),
        MovieItem(
            id = "close_up",
            title = "کلوزآپ (نمای نزدیک)",
            englishTitle = "Close-Up",
            mediaType = MediaType.MOVIE,
            director = "عباس کیارستمی",
            releaseYear = "۱۳۶۸",
            duration = "۸۴ دقیقه",
            imdbRating = 8.2f,
            ageRating = "+۱۰",
            backdropRes = R.drawable.img_classic_cinema_1792095588359,
            genres = listOf("سینمای کلاسیک", "درام و اجتماعی"),
            synopsis = "حسین سبزیان، به دلیل شباهت به محسن مخملباف خود را به خانواده آهن‌خواه معرفی می‌کند تا فیلمی درباره آنها بسازد. در نهایت دستگیر شده و در دادگاهی واقعی با حضور خود مخملباف و کیارستمی به حقیقت ماجرا پرداخته می‌شود.",
            awards = listOf("جزو ۵۰ فیلم برتر تمام دوران تاریخ سینمای جهان در نظرسنجی کارگردانان معتبر جهان"),
            cast = listOf(
                CastMember("حسین سبزیان", "خودش"),
                CastMember("محسن مخملباف", "خودش"),
                CastMember("حسن فرازمند", "خبرنگار"),
                CastMember("ابوالفضل آهن‌خواه", "خودش")
            ),
            isFeatured = false,
            isCriticChoice = true,
            isTrending = false
        )
    )

    val directors = listOf(
        Director("عباس کیارستمی", "کارگردان، فیلمنامه‌نویس، عکاس و شاعر نامدار بین‌المللی و برنده نخل طلای کن.", listOf("طعم گیلاس", "کلوزآپ", "باد ما را خواهد برد", "خانه دوست کجاست؟")),
        Director("اصغر فرهادی", "تنها کارگردان ایرانی برنده دو جایزه اسکار، دارنده نخل طلای کن و خرس طلای برلین.", listOf("جدایی نادر از سیمین", "فروشنده", "درباره الی", "قهرمان")),
        Director("داریوش مهرجویی", "از پایه‌گذاران موج نوی سینمای ایران و خالق برجسته‌ترین آثار اقتباسی سینما.", listOf("گاو", "هامون", "سنتوری", "لیلا", "مهمان مامان")),
        Director("بهرام بیضایی", "نویسنده، پژوهشگر و نمایشنامه‌نویس تراز اول فرهنگ و سینمای ایران.", listOf("باشو غریبه کوچک", "مرگ یزدگرد", "سگ‌کشی", "شاید وقتی دیگر")),
        Director("سعید روستایی", "کارگردان پرانرژی و جوان با نگاهی بی‌پرده به آسیب‌های اجتماعی.", listOf("ابد و یک روز", "متری شیش و نیم", "برادران لیلا"))
    )
}
