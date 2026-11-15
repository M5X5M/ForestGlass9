package com.forestglass.data

enum class Subject(
    val id: String,
    val titleAr: String,
    val subtitleAr: String
) {
    PHYSICS("physics", "الفيزياء", "القوانين والدوائر والموجات"),
    CHEMISTRY("chemistry", "الكيمياء", "الاتزان والحسابات الكيميائية"),
    BIOLOGY("biology", "الأحياء", "الخلية والأنسجة والوراثة"),
    MATH("math", "الرياضيات", "التفاضل والتكامل والقطوع")
}

data class Lesson(
    val id: String,
    val subject: Subject,
    val chapter: String,
    val title: String,
    val teacher: String,
    val youtubeId: String,
    val durationMinutes: Int,
    val summary: String,
    val relatedLawIds: List<String> = emptyList()
)

object LessonRepository {
    val allLessons: List<Lesson> = listOf(
        // الفيزياء — ضع معرفات فيديوهات يوتيوب الحقيقية مكان السلاسل الفارغة أو المعرّفات التجريبية
        Lesson(
            id = "phy_ohm",
            subject = Subject.PHYSICS,
            chapter = "الفصل الثالث: التيار المتناوب والدوائر",
            title = "قانون أوم والمقاومة",
            teacher = "الأستاذ مؤيد سليم الزيدي",
            youtubeId = "",
            durationMinutes = 24,
            summary = "العلاقة بين فرق الجهد والتيار والمقاومة الكهربائية وتطبيقاتها الوزارية.",
            relatedLawIds = listOf("law_ohm", "law_power")
        ),
        Lesson(
            id = "phy_kirchhoff",
            subject = Subject.PHYSICS,
            chapter = "الفصل الثالث: التيار المتناوب والدوائر",
            title = "قانون كيرشوف",
            teacher = "الأستاذ مؤيد سليم الزيدي",
            youtubeId = "",
            durationMinutes = 31,
            summary = "حفظ الشحنة عند العقد وحفظ الطاقة في المسارات المغلقة.",
            relatedLawIds = listOf("law_kcl", "law_kvl")
        ),
        Lesson(
            id = "phy_equiv_r",
            subject = Subject.PHYSICS,
            chapter = "الفصل الثالث: التيار المتناوب والدوائر",
            title = "المقاومة المكافئة",
            teacher = "الأستاذ مؤيد سليم الزيدي",
            youtubeId = "",
            durationMinutes = 27,
            summary = "ربط المقاومات على التوالي والتوازي والربط المختلط.",
            relatedLawIds = listOf("law_r_series", "law_r_parallel")
        ),
        Lesson(
            id = "phy_capacitors",
            subject = Subject.PHYSICS,
            chapter = "الفصل الأول: المتسعات",
            title = "المتسعة ذات الصفيحتين المتوازيتين",
            teacher = "الأستاذ علي السوداني",
            youtubeId = "",
            durationMinutes = 35,
            summary = "السعة الكهربائية والعوامل المؤثرة فيها وتأثير إدخال العازل.",
            relatedLawIds = listOf("law_cap_q", "law_cap_energy")
        ),
        Lesson(
            id = "phy_induction",
            subject = Subject.PHYSICS,
            chapter = "الفصل الثاني: الحث الكهرومغناطيسي",
            title = "قانون فرداي في الحث",
            teacher = "الأستاذ علي السوداني",
            youtubeId = "",
            durationMinutes = 29,
            summary = "القوة الدافعة الكهربائية المحتثة والتغير في الفيض المغناطيسي.",
            relatedLawIds = listOf("law_faraday", "law_self_ind")
        ),
        // الكيمياء
        Lesson(
            id = "chem_hess",
            subject = Subject.CHEMISTRY,
            chapter = "الفصل الأول: الثرموداينمك",
            title = "قانون هيس وحرارة التفاعل",
            teacher = "الأستاذ هاشم الغرباوي",
            youtubeId = "",
            durationMinutes = 32,
            summary = "حساب إنثالبي التفاعل القياسي بطريق غير مباشر.",
            relatedLawIds = emptyList()
        ),
        Lesson(
            id = "chem_kc",
            subject = Subject.CHEMISTRY,
            chapter = "الفصل الثاني: الاتزان الكيميائي",
            title = "ثابت الاتزان Kc و Kp",
            teacher = "الأستاذ هاشم الغرباوي",
            youtubeId = "",
            durationMinutes = 28,
            summary = "العلاقة بين Kc و Kp وحساب التراكيز عند حالة الاتزان.",
            relatedLawIds = emptyList()
        ),
        // الأحياء
        Lesson(
            id = "bio_cell",
            subject = Subject.BIOLOGY,
            chapter = "الفصل الأول: الخلية",
            title = "العضيات الحية في السايتوبلازم",
            teacher = "الأستاذ ماهر نايف",
            youtubeId = "",
            durationMinutes = 26,
            summary = "المايتوكوندريا، الشبكة البلازمية، وجهاز كولجي ومقارناتها الوزارية.",
            relatedLawIds = emptyList()
        ),
        Lesson(
            id = "bio_mitosis",
            subject = Subject.BIOLOGY,
            chapter = "الفصل الأول: الخلية",
            title = "الانقسام الخيطي والاختزالي",
            teacher = "الأستاذ ماهر نايف",
            youtubeId = "",
            durationMinutes = 34,
            summary = "أطوار الانقسام والتعابر الوراثي وأهم التغييرات في كل طور.",
            relatedLawIds = emptyList()
        ),
        // الرياضيات
        Lesson(
            id = "math_complex",
            subject = Subject.MATH,
            chapter = "الفصل الأول: الأعداد المركبة",
            title = "مبرهنة ديموافر وتعميمها",
            teacher = "الأستاذ حيدر وليد",
            youtubeId = "",
            durationMinutes = 38,
            summary = "الصيغة القطبية وحساب الجذور والقوى للأعداد المركبة.",
            relatedLawIds = emptyList()
        ),
        Lesson(
            id = "math_rates",
            subject = Subject.MATH,
            chapter = "الفصل الثالث: تطبيقات التفاضل",
            title = "المعدلات المرتبطة بالزمن",
            teacher = "الأستاذ حيدر وليد",
            youtubeId = "",
            durationMinutes = 40,
            summary = "خطوات الحل القياسية للمسائل الهندسية والمخروطية.",
            relatedLawIds = emptyList()
        )
    )

    fun bySubject(subject: Subject): List<Lesson> =
        allLessons.filter { it.subject == subject }

    fun findById(id: String): Lesson? =
        allLessons.firstOrNull { it.id == id }
}
