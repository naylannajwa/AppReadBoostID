// File: data/local/database/AppDatabase.kt
package com.readboost.id.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.readboost.id.data.local.dao.*
import com.readboost.id.data.model.*
import java.security.MessageDigest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Database(
    entities = [
        Article::class,
        Notes::class,
        UserProgress::class,
        Leaderboard::class,
        AdminUser::class,
        User::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun articleDao(): ArticleDao
    abstract fun notesDao(): NotesDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "readboost_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val instance = INSTANCE
                if (instance != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            populateDatabase(instance)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                val instance = INSTANCE
                if (instance != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Cek apakah database sudah ada artikel, jika belum populate
                            val articleDao = instance.articleDao()
                            val articleCount = articleDao.getAllArticles().first().size
                            if (articleCount == 0) {
                                populateDatabase(instance)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        private suspend fun populateDatabase(database: AppDatabase) {
            val articleDao = database.articleDao()
            val userProgressDao = database.userProgressDao()
            val leaderboardDao = database.leaderboardDao()
            val userDao = database.userDao()

            // Insert dummy articles
            val articles = listOf(
                Article(
                    title = "Memahami Kecerdasan Buatan (AI)",
                    content = """
Kecerdasan Buatan atau Artificial Intelligence (AI) adalah simulasi kecerdasan manusia yang diprogram dalam mesin. AI memungkinkan komputer untuk belajar dari pengalaman, menyesuaikan dengan input baru, dan melakukan tugas-tugas yang biasanya memerlukan kecerdasan manusia.

Jenis-jenis AI meliputi Machine Learning, Deep Learning, dan Neural Networks. Machine Learning adalah subset dari AI yang memungkinkan sistem untuk belajar dari data tanpa diprogram secara eksplisit. Deep Learning menggunakan jaringan neural yang dalam untuk memproses data dengan cara yang mirip dengan otak manusia.

Aplikasi AI dalam kehidupan sehari-hari sangat luas, mulai dari asisten virtual seperti Siri dan Google Assistant, sistem rekomendasi di Netflix dan Spotify, hingga mobil self-driving. AI juga digunakan dalam bidang kesehatan untuk diagnosis penyakit, dalam keuangan untuk deteksi fraud, dan dalam pendidikan untuk personalisasi pembelajaran.

Di Indonesia, pengembangan AI semakin pesat dengan munculnya berbagai startup teknologi dan dukungan pemerintah. Penting bagi kita untuk memahami AI agar dapat memanfaatkannya secara optimal dalam berbagai aspek kehidupan.
                    """.trimIndent(),
                    duration = 5,
                    category = "Teknologi",
                    difficulty = "Dasar",
                    xp = 15,
                    imageUrl = "https://picsum.photos/seed/ai/300/400",
                    createdAt = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L) // 2 hari yang lalu
                ),
                Article(
                    title = "Psikologi Kebahagiaan dalam Kehidupan Sehari-hari",
                    content = """
Kebahagiaan adalah kondisi emosional yang ditandai dengan perasaan positif seperti kepuasan, kegembiraan, dan makna hidup. Penelitian dalam psikologi positif menunjukkan bahwa kebahagiaan bukan hanya ditentukan oleh faktor eksternal seperti kekayaan atau status sosial.

Faktor-faktor yang mempengaruhi kebahagiaan meliputi hubungan sosial yang berkualitas, aktivitas yang bermakna, kesehatan fisik dan mental, serta praktik syukur. Studi menunjukkan bahwa 50% kebahagiaan kita ditentukan oleh genetika, 10% oleh keadaan hidup, dan 40% oleh tindakan yang kita pilih.

Cara meningkatkan kebahagiaan antara lain: praktik mindfulness, olahraga teratur, membangun hubungan sosial yang positif, menetapkan dan mencapai tujuan pribadi, serta berlatih syukur setiap hari. Penelitian juga menunjukkan bahwa membantu orang lain dapat meningkatkan kebahagiaan kita sendiri.

Penting untuk memahami bahwa kebahagiaan bukanlah tujuan akhir, melainkan perjalanan yang terus berlanjut. Dengan memahami faktor-faktor yang mempengaruhi kebahagiaan, kita dapat membuat keputusan yang lebih baik untuk meningkatkan kualitas hidup.
                    """.trimIndent(),
                    duration = 4,
                    category = "Psikologi",
                    difficulty = "Dasar",
                    xp = 12,
                    imageUrl = "https://picsum.photos/seed/psikologi/300/400",
                    createdAt = System.currentTimeMillis() // Hari ini
                ),
                Article(
                    title = "Sejarah Internet: Dari ARPANET hingga Era Modern",
                    content = """
Internet dimulai sebagai proyek militer AS pada tahun 1960-an yang disebut ARPANET. Tujuannya adalah menciptakan jaringan komunikasi yang tahan terhadap serangan nuklir. Pada tahun 1983, ARPANET beralih ke protokol TCP/IP yang masih digunakan hingga saat ini.

World Wide Web diciptakan oleh Tim Berners-Lee pada tahun 1989 di CERN, Swiss. Ini berbeda dengan internet; Web adalah sistem informasi yang berjalan di atas internet. Browser web pertama yang populer adalah Mosaic pada tahun 1993, diikuti oleh Netscape Navigator.

Perkembangan internet sangat pesat sejak tahun 2000-an dengan munculnya Web 2.0, media sosial, cloud computing, dan Internet of Things (IoT). Saat ini, lebih dari 5 miliar orang di dunia menggunakan internet, mengubah cara kita bekerja, berkomunikasi, dan mengakses informasi.

Internet telah merevolusi hampir setiap aspek kehidupan modern, dari pendidikan hingga perdagangan, dari hiburan hingga kesehatan. Memahami sejarah internet membantu kita menghargai teknologi yang kita gunakan setiap hari.
                    """.trimIndent(),
                    duration = 6,
                    category = "Sejarah",
                    difficulty = "Menengah",
                    xp = 18,
                    imageUrl = "https://picsum.photos/seed/internet/300/400",
                    createdAt = System.currentTimeMillis() // Hari ini
                ),
                Article(
                    title = "Fotosintesis: Proses Kehidupan Tumbuhan",
                    content = """
Fotosintesis adalah proses di mana tumbuhan mengubah energi cahaya matahari menjadi energi kimia dalam bentuk glukosa. Proses ini terjadi di kloroplas, khususnya di dalam pigmen hijau yang disebut klorofil.

Fotosintesis terdiri dari dua tahap utama: reaksi terang dan reaksi gelap (siklus Calvin). Reaksi terang terjadi di membran tilakoid, di mana energi cahaya digunakan untuk memecah molekul air dan menghasilkan ATP dan NADPH. Reaksi gelap terjadi di stroma, di mana CO2 diubah menjadi glukosa menggunakan ATP dan NADPH.

Persamaan sederhana fotosintesis: 6CO2 + 6H2O + energi cahaya → C6H12O6 + 6O2. Proses ini sangat penting bagi kehidupan di Bumi karena menghasilkan oksigen yang kita hirup dan menjadi dasar rantai makanan.

Tanpa fotosintesis, kehidupan seperti yang kita kenal tidak akan ada. Proses ini tidak hanya penting untuk tumbuhan, tetapi juga untuk seluruh ekosistem dan kelangsungan hidup makhluk hidup lainnya.
                    """.trimIndent(),
                    duration = 7,
                    category = "Sains",
                    difficulty = "Menengah",
                    xp = 20,
                    imageUrl = "https://picsum.photos/seed/fotosintesis/300/400"
                ),
                Article(
                    title = "Manajemen Waktu untuk Produktivitas Maksimal",
                    content = """
Manajemen waktu adalah keterampilan penting dalam mencapai kesuksesan pribadi dan profesional. Dengan mengelola waktu dengan baik, kita dapat menyelesaikan lebih banyak tugas, mengurangi stres, dan memiliki lebih banyak waktu untuk hal-hal yang penting.

Teknik-teknik manajemen waktu yang efektif meliputi: metode Pomodoro (bekerja 25 menit, istirahat 5 menit), prioritas tugas dengan matriks Eisenhower, dan teknik time blocking. Penting juga untuk menghindari prokrastinasi dengan memecah tugas besar menjadi langkah-langkah kecil.

Kunci sukses manajemen waktu adalah konsistensi dan disiplin. Mulailah dengan membuat jadwal harian, tetapkan tujuan yang realistis, dan evaluasi progres secara berkala. Ingat bahwa manajemen waktu bukan tentang mengisi setiap menit dengan pekerjaan, tetapi tentang menggunakan waktu secara bijak.

Dengan menguasai manajemen waktu, kita dapat mencapai keseimbangan antara pekerjaan dan kehidupan pribadi, meningkatkan produktivitas, dan mengurangi stres dalam kehidupan sehari-hari.
                    """.trimIndent(),
                    duration = 3,
                    category = "Motivasi",
                    difficulty = "Dasar",
                    xp = 10,
                    imageUrl = "https://picsum.photos/seed/manajemen/300/400"
                ),
                Article(
                    title = "Blockchain: Teknologi di Balik Cryptocurrency",
                    content = """
Blockchain adalah teknologi distributed ledger yang mencatat transaksi secara aman dan transparan. Setiap blok dalam blockchain berisi data transaksi, timestamp, dan hash dari blok sebelumnya, membentuk rantai yang tidak dapat diubah.

Karakteristik utama blockchain meliputi: desentralisasi (tidak ada otoritas pusat), transparansi (semua transaksi dapat dilihat), immutability (data tidak dapat diubah setelah dicatat), dan keamanan (menggunakan kriptografi). Bitcoin adalah aplikasi blockchain yang paling terkenal, tetapi teknologi ini memiliki potensi di banyak bidang lain.

Aplikasi blockchain di luar cryptocurrency meliputi: supply chain management, voting elektronik, smart contracts, manajemen identitas digital, dan rekam medis. Di Indonesia, beberapa perusahaan dan pemerintah mulai mengeksplorasi penggunaan blockchain untuk berbagai aplikasi.

Memahami blockchain penting karena teknologi ini berpotensi mengubah cara kita bertransaksi, berbagi informasi, dan membangun kepercayaan dalam sistem digital. Meskipun masih dalam tahap perkembangan, blockchain menawarkan solusi untuk banyak masalah dalam dunia digital.
                    """.trimIndent(),
                    duration = 8,
                    category = "Teknologi",
                    difficulty = "Lanjut",
                    xp = 25,
                    imageUrl = "https://picsum.photos/seed/blockchain/300/400"
                ),
                Article(
                    title = "Budaya Gotong Royong di Indonesia",
                    content = """
Gotong royong adalah nilai budaya Indonesia yang mencerminkan semangat kebersamaan dan saling membantu dalam masyarakat. Konsep ini telah menjadi bagian integral dari kehidupan sosial masyarakat Indonesia sejak zaman dahulu.

Praktik gotong royong dapat ditemukan dalam berbagai aspek kehidupan, mulai dari membangun rumah, membersihkan lingkungan, hingga merayakan acara-acara penting. Di era modern, semangat gotong royong tetap relevan dan penting untuk memperkuat solidaritas sosial.

Nilai-nilai gotong royong meliputi: kerelawanan, kebersamaan, tanggung jawab bersama, dan kepedulian terhadap sesama. Dalam konteks pembangunan nasional, gotong royong menjadi modal sosial yang penting untuk mencapai tujuan bersama.

Melestarikan budaya gotong royong di era digital memerlukan adaptasi dan kreativitas. Misalnya, melalui platform digital untuk menggalang dana, volunteer matching, atau kampanye sosial. Dengan menjaga semangat gotong royong, kita dapat membangun masyarakat yang lebih solid dan harmonis.
                    """.trimIndent(),
                    duration = 4,
                    category = "Sosial & Budaya",
                    difficulty = "Dasar",
                    xp = 12,
                    imageUrl = "https://picsum.photos/seed/gotongroyong/300/400"
                ),
                Article(
                    title = "Perubahan Iklim dan Dampaknya",
                    content = """
Perubahan iklim adalah perubahan jangka panjang dalam pola cuaca dan suhu rata-rata di Bumi. Penyebab utama perubahan iklim adalah aktivitas manusia, terutama pembakaran bahan bakar fosil yang menghasilkan gas rumah kaca seperti CO2.

Dampak perubahan iklim sudah terasa di berbagai belahan dunia: kenaikan suhu global, pencairan es di kutub, kenaikan permukaan laut, perubahan pola curah hujan, dan peningkatan frekuensi cuaca ekstrem. Di Indonesia, dampaknya termasuk banjir, kekeringan, dan ancaman terhadap biodiversitas.

Mitigasi perubahan iklim memerlukan aksi dari semua pihak: pemerintah, industri, dan individu. Langkah-langkah yang dapat dilakukan meliputi: mengurangi emisi karbon, menggunakan energi terbarukan, menanam pohon, mengurangi konsumsi daging, dan memilih transportasi ramah lingkungan.

Adaptasi terhadap perubahan iklim juga penting, termasuk membangun infrastruktur yang tahan bencana, mengembangkan varietas tanaman yang tahan kekeringan, dan meningkatkan kesadaran masyarakat. Setiap individu memiliki peran dalam mengatasi perubahan iklim untuk masa depan yang lebih baik.
                    """.trimIndent(),
                    duration = 6,
                    category = "Sains",
                    difficulty = "Menengah",
                    xp = 18,
                    imageUrl = "https://picsum.photos/seed/iklim/300/400"
                ),
                // Artikel tambahan untuk kategori yang ada di HomeScreen
                Article(
                    title = "Seni Rupa Modern Indonesia",
                    content = """
Seni rupa modern Indonesia telah mengalami perkembangan pesat sejak era kemerdekaan. Gerakan seni modern Indonesia dimulai dengan munculnya berbagai aliran seperti naturalisme, ekspresionisme, dan abstrak yang mengadaptasi teknik Barat dengan nuansa lokal.

Tokoh-tokoh penting dalam seni rupa modern Indonesia antara lain Raden Saleh, Affandi, S. Sudjojono, dan Basuki Abdullah. Mereka tidak hanya menciptakan karya yang indah, tetapi juga menyampaikan pesan sosial dan budaya melalui seni mereka. Karya-karya mereka mencerminkan perjuangan, harapan, dan identitas bangsa Indonesia.

Di era kontemporer, seni rupa Indonesia semakin beragam dengan munculnya seniman-seniman muda yang mengeksplorasi media baru seperti digital art, instalasi, dan performance art. Galeri seni dan museum di Jakarta, Yogyakarta, dan Bandung menjadi pusat perkembangan seni rupa modern Indonesia.

Memahami seni rupa modern Indonesia penting untuk menghargai warisan budaya dan kreativitas bangsa. Seni tidak hanya estetika, tetapi juga bentuk ekspresi yang mencerminkan kondisi sosial dan sejarah bangsa.
                    """.trimIndent(),
                    duration = 5,
                    category = "Seni",
                    difficulty = "Dasar",
                    xp = 15,
                    imageUrl = "https://picsum.photos/seed/senirupa/300/400"
                ),
                Article(
                    title = "Strategi Pemasaran Digital untuk Bisnis Kecil",
                    content = """
Pemasaran digital telah menjadi kebutuhan penting bagi bisnis kecil di era modern. Dengan semakin banyaknya konsumen yang berbelanja online, bisnis kecil perlu mengadopsi strategi pemasaran digital yang efektif untuk bersaing di pasar.

Strategi pemasaran digital yang efektif meliputi: optimasi website untuk mobile, penggunaan media sosial untuk engagement dengan pelanggan, email marketing untuk retention, dan content marketing untuk membangun brand awareness. Platform seperti Instagram, Facebook, dan TikTok menawarkan peluang besar untuk bisnis kecil dengan budget terbatas.

SEO (Search Engine Optimization) juga penting untuk meningkatkan visibilitas bisnis di mesin pencari. Dengan mengoptimalkan konten website, bisnis kecil dapat menarik pelanggan potensial yang mencari produk atau jasa mereka secara organik.

Analisis data dan tracking performa kampanye sangat penting untuk mengukur ROI (Return on Investment). Dengan tools seperti Google Analytics, bisnis kecil dapat memahami perilaku pelanggan dan menyesuaikan strategi mereka.
                    """.trimIndent(),
                    duration = 6,
                    category = "Bisnis",
                    difficulty = "Menengah",
                    xp = 18,
                    imageUrl = "https://picsum.photos/seed/pemasaran/300/400"
                ),
                Article(
                    title = "Mitos dan Legenda Nusantara",
                    content = """
Nusantara kaya akan mitos dan legenda yang telah diturunkan dari generasi ke generasi. Cerita-cerita ini tidak hanya menghibur, tetapi juga mengandung nilai-nilai moral, kearifan lokal, dan pengetahuan tentang budaya Indonesia.

Beberapa legenda terkenal meliputi: Malin Kundang dari Sumatera Barat yang mengajarkan pentingnya menghormati orang tua, Roro Jonggrang yang berkisah tentang cinta dan pengorbanan, Timun Mas yang mengajarkan keberanian menghadapi kesulitan, dan Jaka Tarub yang bercerita tentang kesederhanaan dan kebijaksanaan.

Cerita-cerita ini seringkali mengandung elemen fantasi dan magis, seperti dewa-dewa, makhluk halus, dan kekuatan supranatural. Namun di balik elemen fantasi tersebut, tersimpan pesan-pesan penting tentang kehidupan, moralitas, dan hubungan manusia dengan alam dan sesama.

Memahami mitos dan legenda Nusantara penting untuk melestarikan warisan budaya dan menanamkan nilai-nilai luhur kepada generasi muda. Cerita-cerita ini juga dapat menjadi sumber inspirasi untuk karya sastra dan seni kontemporer.
                    """.trimIndent(),
                    duration = 4,
                    category = "Fantasi",
                    difficulty = "Dasar",
                    xp = 12,
                    imageUrl = "https://picsum.photos/seed/legenda/300/400"
                ),
                Article(
                    title = "Sistem Tata Surya dan Planet-Planetnya",
                    content = """
Tata surya adalah sistem yang terdiri dari Matahari sebagai bintang pusat dan semua benda langit yang mengorbit di sekitarnya. Sistem ini terbentuk sekitar 4,6 miliar tahun yang lalu dari awan debu dan gas yang berputar.

Delapan planet dalam tata surya dapat dibagi menjadi dua kelompok: planet dalam (Merkurius, Venus, Bumi, Mars) yang berbatu, dan planet luar (Jupiter, Saturnus, Uranus, Neptunus) yang merupakan raksasa gas. Setiap planet memiliki karakteristik unik, seperti suhu, atmosfer, dan bulan yang berbeda-beda.

Bumi adalah satu-satunya planet yang diketahui memiliki kehidupan. Planet ini memiliki atmosfer yang kaya oksigen, air dalam bentuk cair, dan jarak yang tepat dari Matahari sehingga suhunya cocok untuk kehidupan. Bulan, satelit alami Bumi, juga memainkan peran penting dalam stabilitas iklim planet kita.

Memahami tata surya membantu kita menghargai keunikan Bumi dan pentingnya menjaga planet kita. Pengetahuan tentang tata surya juga mendorong eksplorasi ruang angkasa dan pencarian planet lain yang mungkin bisa dihuni.
                    """.trimIndent(),
                    duration = 7,
                    category = "Ilmiah",
                    difficulty = "Menengah",
                    xp = 20,
                    imageUrl = "https://picsum.photos/seed/tatasurya/300/400"
                ),
                Article(
                    title = "Cloud Computing: Teknologi Masa Depan",
                    content = """
Cloud computing adalah teknologi yang memungkinkan penyimpanan dan pengolahan data melalui internet, bukan menggunakan hard drive lokal. Layanan cloud computing menyediakan akses ke sumber daya komputasi seperti server, storage, database, dan aplikasi melalui internet.

Keuntungan utama cloud computing meliputi: skalabilitas yang mudah, biaya operasional yang lebih rendah, akses dari mana saja, backup dan recovery yang otomatis, serta update dan maintenance yang terkelola. Layanan cloud computing utama termasuk Infrastructure as a Service (IaaS), Platform as a Service (PaaS), dan Software as a Service (SaaS).

Perusahaan besar seperti Amazon (AWS), Microsoft (Azure), dan Google (Cloud Platform) menyediakan layanan cloud yang digunakan oleh jutaan bisnis di seluruh dunia. Di Indonesia, adopsi cloud computing semakin meningkat seiring dengan transformasi digital di berbagai sektor.

Cloud computing telah merevolusi cara bisnis beroperasi, memungkinkan startup untuk bersaing dengan perusahaan besar, dan memfasilitasi kerja remote. Memahami cloud computing penting untuk tetap relevan di era digital ini.
                    """.trimIndent(),
                    duration = 6,
                    category = "Teknologi",
                    difficulty = "Menengah",
                    xp = 18,
                    imageUrl = "https://picsum.photos/seed/cloud/300/400"
                ),
                Article(
                    title = "Filosofi Seni Wayang: Simbol dan Makna",
                    content = """
Wayang adalah seni pertunjukan tradisional Indonesia yang memiliki nilai filosofis yang mendalam. Setiap karakter wayang memiliki makna simbolis yang mencerminkan sifat manusia, moralitas, dan hubungan antara baik dan buruk.

Tokoh-tokoh wayang seperti Pandawa Lima melambangkan kebaikan dan kebijaksanaan, sementara Kurawa mewakili kejahatan dan keserakahan. Kisah-kisah wayang seperti Mahabharata dan Ramayana mengajarkan nilai-nilai seperti kebenaran, keadilan, keberanian, dan pengorbanan.

Pertunjukan wayang tidak hanya hiburan, tetapi juga media pendidikan moral dan spiritual. Dalang, sebagai pemimpin pertunjukan, memiliki peran penting dalam menyampaikan pesan-pesan moral melalui cerita yang dibawakannya. Gamelan sebagai musik pengiring juga memiliki makna tersendiri.

Memahami filosofi wayang membantu kita menghargai kekayaan budaya Indonesia dan mengambil pelajaran berharga dari cerita-cerita klasik. Wayang tetap relevan hingga hari ini sebagai bentuk seni yang mengajarkan nilai-nilai luhur kepada masyarakat modern.
                    """.trimIndent(),
                    duration = 5,
                    category = "Seni",
                    difficulty = "Dasar",
                    xp = 15,
                    imageUrl = "https://picsum.photos/seed/wayang/300/400"
                ),
                Article(
                    title = "Startup Unicorn Indonesia: Kisah Sukses",
                    content = """
Indonesia telah melahirkan beberapa startup unicorn (valuasi lebih dari $1 miliar) yang menjadi inspirasi bagi pengusaha muda. Startup-startup ini menunjukkan bahwa Indonesia memiliki potensi besar di dunia teknologi dan bisnis digital.

Beberapa startup unicorn Indonesia termasuk Gojek, Tokopedia (sekarang GoTo), Traveloka, Bukalapak, dan OVO. Masing-masing startup ini mengidentifikasi masalah spesifik di masyarakat Indonesia dan menciptakan solusi inovatif melalui teknologi. Gojek memecahkan masalah transportasi dan logistik, sementara Tokopedia mempermudah perdagangan online.

Kunci sukses startup-startup ini meliputi: fokus pada masalah lokal, eksekusi yang cepat, teknologi yang user-friendly, dan pembiayaan yang tepat. Mereka juga membangun ekosistem yang tidak hanya menguntungkan bisnis mereka, tetapi juga memberdayakan banyak orang melalui program driver dan seller.

Kisah sukses startup unicorn Indonesia menginspirasi generasi muda untuk berwirausaha dan menggunakan teknologi untuk menciptakan solusi yang berdampak positif bagi masyarakat. Startup ecosystem Indonesia terus berkembang dengan munculnya startup-startup baru yang berpotensi menjadi unicorn berikutnya.
                    """.trimIndent(),
                    duration = 7,
                    category = "Bisnis",
                    difficulty = "Menengah",
                    xp = 20,
                    imageUrl = "https://picsum.photos/seed/startup/300/400"
                ),
                Article(
                    title = "Dunia Paralel dalam Sains dan Fiksi",
                    content = """
Konsep dunia paralel atau multiverse telah lama menjadi topik menarik baik dalam sains maupun fiksi. Dalam fisika teoretis, teori multiverse muncul dari interpretasi mekanika kuantum dan teori string, yang menyatakan bahwa mungkin ada banyak alam semesta yang eksis secara bersamaan.

Dalam fiksi, dunia paralel sering menjadi setting untuk cerita fantasi dan sci-fi yang menarik. Konsep ini memungkinkan penulis mengeksplorasi "what if" - apa yang terjadi jika sejarah berjalan berbeda, atau jika karakter hidup di dunia dengan aturan fisika yang berbeda. Novel-novel seperti "The Chronicles of Narnia" dan "His Dark Materials" menggunakan konsep dunia paralel.

Dalam sains, teori multiverse masih spekulatif dan belum bisa dibuktikan secara empiris. Namun, konsep ini membantu ilmuwan memahami beberapa misteri fisika modern, seperti fine-tuning konstanta fisika dan masalah pengamat dalam mekanika kuantum.

Memahami konsep dunia paralel, baik dari perspektif sains maupun fiksi, membuka wawasan tentang kemungkinan-kemungkinan yang ada di alam semesta. Konsep ini juga mengajarkan kita untuk berpikir kreatif dan terbuka terhadap berbagai kemungkinan.
                    """.trimIndent(),
                    duration = 6,
                    category = "Fantasi",
                    difficulty = "Menengah",
                    xp = 18,
                    imageUrl = "https://picsum.photos/seed/paralel/300/400"
                ),
                Article(
                    title = "DNA dan Genetika: Kode Kehidupan",
                    content = """
DNA (Deoxyribonucleic Acid) adalah molekul yang mengandung instruksi genetik untuk perkembangan, fungsi, pertumbuhan, dan reproduksi semua organisme hidup. Struktur DNA yang berbentuk double helix ditemukan oleh James Watson dan Francis Crick pada tahun 1953.

DNA terdiri dari empat basa nukleotida: Adenin (A), Timin (T), Guanin (G), dan Sitosin (C). Urutan basa-basa ini menentukan karakteristik organisme. Gen adalah segmen DNA yang mengkode protein spesifik, yang pada gilirannya menentukan sifat-sifat organisme.

Genetika adalah ilmu yang mempelajari hereditas dan variasi karakteristik organisme. Pengetahuan tentang genetika telah mengubah banyak aspek kehidupan, dari kedokteran (genetic testing, terapi gen) hingga pertanian (rekayasa genetika tanaman). Human Genome Project berhasil memetakan seluruh genom manusia pada tahun 2003.

Memahami DNA dan genetika penting untuk memahami bagaimana kehidupan bekerja, mengembangkan pengobatan untuk penyakit genetik, dan membuat keputusan informatif tentang kesehatan. Penelitian genetika terus berkembang dan membuka kemungkinan baru di berbagai bidang.
                    """.trimIndent(),
                    duration = 8,
                    category = "Ilmiah",
                    difficulty = "Lanjut",
                    xp = 25,
                    imageUrl = "https://picsum.photos/seed/dna/300/400"
                ),
                Article(
                    title = "Internet of Things (IoT): Menghubungkan Segala Sesuatu",
                    content = """
Internet of Things (IoT) adalah konsep di mana perangkat sehari-hari terhubung ke internet dan dapat berkomunikasi satu sama lain. Dari smart home devices hingga sensor industri, IoT mengubah cara kita hidup dan bekerja.

Aplikasi IoT sangat luas: smart home (lampu, AC, kulkas yang bisa dikontrol via smartphone), smart city (lampu lalu lintas otomatis, monitoring polusi), pertanian cerdas (sensor tanah, irigasi otomatis), kesehatan (wearable devices untuk monitoring kesehatan), dan industri (predictive maintenance pada mesin).

Keuntungan IoT meliputi efisiensi energi, otomasi tugas rutin, pengambilan keputusan berbasis data real-time, dan peningkatan kualitas hidup. Namun, IoT juga menghadapi tantangan seperti keamanan data, privasi, interoperabilitas antar perangkat, dan manajemen data besar.

Di Indonesia, adopsi IoT semakin meningkat seiring dengan perkembangan infrastruktur internet dan 5G. Startup dan perusahaan lokal mulai mengembangkan solusi IoT untuk berbagai sektor. Memahami IoT penting untuk mengikuti perkembangan teknologi dan memanfaatkannya dalam kehidupan sehari-hari.
                    """.trimIndent(),
                    duration = 7,
                    category = "Teknologi",
                    difficulty = "Menengah",
                    xp = 20,
                    imageUrl = "https://picsum.photos/seed/iot/300/400"
                ),
                Article(
                    title = "Ekosistem Hutan Hujan Tropis Indonesia",
                    content = """
Indonesia memiliki salah satu ekosistem hutan hujan tropis terbesar di dunia, yang menjadi rumah bagi keanekaragaman hayati yang luar biasa. Hutan hujan tropis Indonesia mencakup sekitar 10% dari total hutan hujan tropis dunia dan merupakan habitat bagi ribuan spesies flora dan fauna endemik.

Keanekaragaman hayati hutan Indonesia sangat mengesankan: lebih dari 17.000 pulau menjadi rumah bagi berbagai ekosistem unik. Hutan Kalimantan, Sumatera, dan Papua adalah beberapa hotspot keanekaragaman hayati dunia, dengan spesies seperti orang utan, harimau Sumatera, badak Jawa, dan berbagai jenis burung endemik.

Hutan hujan tropis memainkan peran penting dalam regulasi iklim global, penyerapan karbon, siklus air, dan menyediakan sumber daya alam untuk jutaan orang. Namun, hutan Indonesia menghadapi ancaman serius dari deforestasi, kebakaran hutan, dan perubahan iklim.

Konservasi hutan hujan tropis Indonesia sangat penting untuk menjaga keanekaragaman hayati, memitigasi perubahan iklim, dan memastikan keberlanjutan sumber daya alam. Program konservasi, reboisasi, dan pengelolaan hutan berkelanjutan perlu ditingkatkan untuk melindungi warisan alam yang berharga ini.
                    """.trimIndent(),
                    duration = 6,
                    category = "Ilmiah",
                    difficulty = "Menengah",
                    xp = 18,
                    imageUrl = "https://picsum.photos/seed/hutan/300/400"
                )
            )

            // Clear existing articles first to avoid duplicates
            articleDao.deleteAllArticles()
            // Insert all articles
            articles.forEach { articleDao.insertArticle(it) }

            // Insert initial user progress
            val userProgress = UserProgress(
                id = 1,
                totalXP = 0,
                streakDays = 0,
                dailyTarget = 5,
                lastReadDate = 0L,
                lastStreakDate = 0L,
                dailyXPEarned = 0,
                dailyReadingMinutes = 0
            )
            userProgressDao.insertUserProgress(userProgress)

            // Insert dummy leaderboard data
            val leaderboards = listOf(
                Leaderboard(username = "Budi Santoso", totalXP = 850, rank = 1),
                Leaderboard(username = "Siti Nurhaliza", totalXP = 720, rank = 2),
                Leaderboard(username = "Ahmad Fauzi", totalXP = 680, rank = 3),
                Leaderboard(username = "Rina Wijaya", totalXP = 550, rank = 4),
                Leaderboard(username = "Dedi Kurniawan", totalXP = 490, rank = 5),
                Leaderboard(username = "Maya Puspita", totalXP = 430, rank = 6),
                Leaderboard(username = "Eko Prasetyo", totalXP = 380, rank = 7),
                Leaderboard(username = "Indah Permata", totalXP = 320, rank = 8),
                Leaderboard(username = "Hendra Wijaya", totalXP = 280, rank = 9),
                Leaderboard(username = "Fitri Handayani", totalXP = 250, rank = 10)
            )
            leaderboardDao.insertAllLeaderboard(leaderboards)

            // Insert dummy users for testing
            val dummyUsers = listOf(
                User(
                    username = "user",
                    email = "user@test.com",
                    passwordHash = hashPassword("user123"),
                    fullName = "User Test",
                    role = "user"
                ),
                User(
                    username = "admin",
                    email = "admin@test.com",
                    passwordHash = hashPassword("admin123"),
                    fullName = "Admin Test",
                    role = "admin"
                ),
                User(
                    username = "nayla",
                    email = "nayla@test.com",
                    passwordHash = hashPassword("nayla123"),
                    fullName = "Nayla Jihana",
                    role = "user"
                )
            )
            dummyUsers.forEach { userDao.insertUser(it) }
        }

        private fun hashPassword(password: String): String {
            val bytes = password.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }
    }
}