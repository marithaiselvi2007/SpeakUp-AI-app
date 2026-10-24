package com.example.util

import java.util.Locale

/**
 * Provides accurate Tamil translations / explanations for questions in SpeakUp AI Daily Challenges and Topics.
 * Only shown on user demand ("தமிழில் அர்த்தம்" / "Tamil Meaning") to help understand the question without altering
 * the English question or answer requirement.
 */
object TamilQuestionTranslator {

    private val directQuestionMap: Map<String, String> = mapOf(
        // Day 1
        "Introduce yourself clearly by stating your full name, where you grew up, and your family background." to
                "உங்கள் முழுப் பெயர், நீங்கள் வளர்ந்த இடம் மற்றும் உங்கள் குடும்பப் பின்னணியைக் கூறி உங்களைத் தெளிவாக அறிமுகப்படுத்துங்கள்.",
        "Describe your educational background, current job or studies, and what you do on a daily basis." to
                "உங்கள் கல்விப் பின்னணி, தற்போதைய வேலை அல்லது படிப்பு மற்றும் நீங்கள் தினசரி என்ன செய்கிறீர்கள் என்பதை விவரியுங்கள்.",
        "Explain your primary passions, personal goals, and what you hope to accomplish in the next two years." to
                "உங்கள் முக்கிய ஆர்வங்கள், தனிப்பட்ட இலக்குகள் மற்றும் அடுத்த இரண்டு ஆண்டுகளில் நீங்கள் என்ன சாதிக்க விரும்புகிறீர்கள் என்பதை விளக்குங்கள்.",

        // Day 2
        "What is the name of your hometown or city, and what makes its neighborhood atmosphere unique and lively?" to
                "உங்கள் சொந்த ஊர் அல்லது நகரத்தின் பெயர் என்ன? அதன் சுற்றுப்புற சூழலை தனித்துவமாகவும் உற்சாகமாகவும் மாற்றுவது எது?",
        "Describe the most famous landmark, historic monument, or scenic spot in your area and why people love visiting it." to
                "உங்கள் பகுதியில் உள்ள மிகவும் பிரபலமான வரலாற்றுச் சின்னம், நினைவுச்சின்னம் அல்லது இயற்கை காட்சியை விவரியுங்கள்; மக்கள் அங்கு செல்ல ஏன் விரும்புகிறார்கள்?",
        "Talk about the local traditions, cultural festivals, and delicious regional dishes famous in your hometown." to
                "உங்கள் சொந்த ஊரில் பிரபலமான உள்ளூர் பாரம்பரியங்கள், கலாச்சார திருவிழாக்கள் மற்றும் சுவையான பிராந்திய உணவுகள் பற்றி பேசுங்கள்.",

        // Day 3
        "Describe your typical morning routine from the moment your alarm rings until you leave for work or study." to
                "அலாரம் அடித்ததில் இருந்து வேலைக்கு அல்லது படிப்புக்கு கிளம்பும் வரை உங்கள் வழக்கமான காலை நேரப் பழக்கங்களை விவரியுங்கள்.",
        "How do you organize your work or study hours to stay productive and avoid digital distractions?" to
                "அதிக பயனுள்ளதாக இருக்கவும் கவனச்சிதறல்களைத் தவிர்க்கவும் உங்கள் வேலை அல்லது படிப்பு நேரத்தை எவ்வாறு திட்டமிடுகிறீர்கள்?",
        "Explain what you do in the evening to relax, recharge, and prepare yourself for the next day." to
                "மாலையில் மனதை அமைதிப்படுத்தவும், அடுத்த நாளுக்கு உங்களை தயார்படுத்தவும் என்ன செய்கிறீர்கள் என்பதை விளக்குங்கள்.",

        // Day 4
        "What is your all-time favorite hobby or creative pastime, and how did you first get interested in it?" to
                "உங்களுக்கு எப்போதும் பிடித்த பொழுதுபோக்கு எது? அதில் உங்களுக்கு எப்படி முதன்முதலில் ஆர்வம் ஏற்பட்டது?",
        "How does engaging in this hobby help you relieve stress and develop creative problem-solving skills?" to
                "இந்தப் பொழுதுபோக்கில் ஈடுபடுவது உங்கள் மன அழுத்தத்தைக் குறைக்கவும், புதிய சிந்தனைத் திறன்களை வளர்க்கவும் எப்படி உதவுகிறது?",
        "Describe an enjoyable project, match, or creative activity that you participated in recently." to
                "நீங்கள் சமீபத்தில் பங்கேற்ற ஒரு மகிழ்ச்சியான திட்டம், விளையாட்டு போட்டி அல்லது படைப்பாற்றல் செயல்பாடு பற்றி விவரியுங்கள்.",

        // Day 5
        "What is your absolute favorite comfort food or traditional dish, and what ingredients make it delicious?" to
                "உங்களுக்கு மிகவும் பிடித்த பாரம்பரிய உணவு எது? அதில் சேர்க்கப்படும் எந்தப் பொருட்கள் அதைச் சுவையாக மாற்றுகின்றன?",
        "Describe a memorable dining experience at a restaurant or family feast that you still remember fondly." to
                "உணவகத்திலோ அல்லது குடும்ப விருந்திலோ நீங்கள் இன்றும் மகிழ்ச்சியுடன் நினைவில் வைத்திருக்கும் ஒரு மறக்கமுடியாத உணவு அனுபவத்தை விவரியுங்கள்.",
        "Do you enjoy cooking? Describe a simple dish you know how to prepare step-by-step." to
                "உங்களுக்கு சமைக்கப் பிடிக்குமா? உங்களுக்கு தெரிந்த ஒரு எளிய உணவை எப்படி செய்வது என்று படிப்படியாக விவரியுங்கள்.",

        // Day 6
        "Tell us about an unforgettable travel destination you visited and what made that trip special." to
                "நீங்கள் சென்றுவந்த ஒரு மறக்கமுடியாத சுற்றுலாத் தலத்தைப் பற்றியும், அந்தப் பயணம் ஏன் சிறப்பானது என்பதைப் பற்றியும் கூறுங்கள்.",
        "If you could travel anywhere in the world next month with an unlimited budget, where would you go and why?" to
                "அடுத்த மாதம் போதிய பண வசதியுடன் உலகில் எங்கு வேண்டுமானாலும் பயணம் செய்ய முடிந்தால், எங்கு செல்வீர்கள், ஏன்?",
        "How does traveling to new places broaden your perspective on people, cultures, and life?" to
                "புதிய இடங்களுக்குப் பயணம் செய்வது மனிதர்கள், கலாச்சாரம் மற்றும் வாழ்க்கை பற்றிய உங்கள் பார்வையை எவ்வாறு விரிவுபடுத்துகிறது?",

        // Day 7
        "What is your dream job or ideal career role, and what inspired you to pursue this path?" to
                "உங்கள் கனவு வேலை அல்லது இலட்சியப் பணி எது? இந்த வழியைத் தேர்ந்தெடுக்க உங்களுக்கு என்ன தூண்டுதலாக இருந்தது?",
        "Where do you see yourself professionally five years from now, and what leadership role do you aspire to hold?" to
                "இன்னும் ஐந்து ஆண்டுகளில் உங்களை எந்த தொழில் நிலையில் பார்க்கிறீர்கள்? என்ன தலைமைப் பொறுப்பை வகிக்க விரும்புகிறீர்கள்?",
        "What new technical skills or soft skills are you actively learning right now to advance your career?" to
                "உங்கள் வாழ்க்கைத் தொழிலை முன்னெடுத்துச் செல்ல இப்போது நீங்கள் தீவிரமாகக் கற்றுக்கொள்ளும் புதிய திறன்கள் யாவை?",

        // Day 8
        "Describe your best friend or closest mentor. What unique qualities do you admire most in them?" to
                "உங்கள் சிறந்த நண்பர் அல்லது வழிகாட்டியைப் பற்றி விவரியுங்கள். அவர்களிடம் நீங்கள் மிகவும் ரசிக்கும் தனித்துவமான குணங்கள் என்ன?",
        "Share a story of a time when a friend or family member supported you through a difficult situation." to
                "ஒரு கடினமான சூழ்நிலையில் ஒரு நண்பர் அல்லது குடும்ப உறுப்பினர் உங்களுக்கு ஆதரவாக இருந்த ஒரு நிகழ்வைப் பகிருங்கள்.",
        "In your opinion, what are the three most important qualities that make a long-lasting, trustworthy relationship?" to
                "உங்கள் கருத்துப்படி, நீண்டகால, நம்பிக்கையான உறவை உருவாக்குவதற்கு முக்கியமான மூன்று குணங்கள் என்ன?",

        // Day 9
        "Which modern digital device or mobile application has changed your daily life the most, and how?" to
                "எந்த நவீன டிஜிட்டல் கருவி அல்லது மொபைல் செயலி உங்கள் அன்றாட வாழ்க்கையை மிகவும் மாற்றியமைத்துள்ளது, எப்படி?",
        "What are the biggest benefits and potential challenges of Artificial Intelligence in education and jobs?" to
                "கல்வி மற்றும் வேலைவாய்ப்பில் செயற்கை நுண்ணறிவின் (AI) மிகப்பெரிய நன்மைகள் மற்றும் சவால்கள் என்ன?",
        "How do you strike a healthy balance between using digital gadgets and enjoying offline real-world life?" to
                "டிஜிட்டல் சாதனங்களைப் பயன்படுத்துவதற்கும் நிஜ வாழ்க்கையை அனுபவிப்பதற்கும் இடையே எவ்வாறு ஆரோக்கியமான சமநிலையைப் பேணுகிறீர்கள்?",

        // Day 10
        "Tell a story about an unexpected obstacle or difficult problem you faced in school, college, or work." to
                "பள்ளி, கல்லூரி அல்லது வேலையில் நீங்கள் சந்தித்த ஒரு எதிர்பாராத சவால் அல்லது கடினமான பிரச்சினையைப் பற்றிய ஒரு கதையைக் கூறுங்கள்.",
        "What mindset, steps, or help from others enabled you to overcome that challenge successfully?" to
                "எந்த மனப்பான்மை, நடவடிக்கைகள் அல்லது பிறரின் உதவி அந்தச் சவாலை வெற்றிகரமாக சமாளிக்க உங்களுக்கு உதவியது?",
        "What did that difficult experience teach you about resilience, mental courage, and self-confidence?" to
                "அந்தக் கடினமான அனுபவம் மன உறுதி, தைரியம் மற்றும் தன்னம்பிக்கை பற்றி உங்களுக்கு என்ன கற்றுக் கொடுத்தது?",

        // Day 11
        "What is your all-time favorite movie, book, or TV series, and what is its core moral message?" to
                "உங்களுக்கு மிகவும் பிடித்த திரைப்படம், புத்தகம் அல்லது தொலைக்காட்சி தொடர் எது, அதன் முக்கிய கருத்து என்ன?",
        "How does music or cinema influence your emotions, imagination, and day-to-day motivation?" to
                "இசை அல்லது சினிமா உங்கள் உணர்வுகள், கற்பனை மற்றும் அன்றாட உத்வேகத்தை எவ்வாறு பாதிக்கிறது?",
        "If you were a film director, what compelling story or social issue would you want to create a film about?" to
                "நீங்கள் ஒரு திரைப்பட இயக்குநராக இருந்தால், எந்தக் கதை அல்லது சமூகப் பிரச்சினை பற்றி படம் எடுக்க விரும்புவீர்கள்?",

        // Day 12
        "What physical exercises, sports, or wellness habits do you practice regularly to keep your body energetic and healthy?" to
                "உங்கள் உடலை ஆரோக்கியமாகவும் சுறுசுறுப்பாகவும் வைத்திருக்க நீங்கள் தவறாமல் செய்யும் உடற்பயிற்சிகள் அல்லது பழக்கங்கள் என்ன?",
        "How do good sleep, nutritious home-cooked food, and daily hydration affect your mood and concentration?" to
                "நல்ல தூக்கம், சத்தான வீட்டு உணவு மற்றும் போதிய தண்ணீர் குடிப்பது உங்கள் மனநிலை மற்றும் கவனத்தை எவ்வாறு பாதிக்கிறது?",
        "What changes would you suggest to people in your community to live a more active and stress-free lifestyle?" to
                "உங்கள் சமூகத்தில் உள்ள மக்கள் ஆரோக்கியமாகவும் மன அழுத்தமின்றியும் வாழ நீங்கள் என்ன மாற்றங்களைப் பரிந்துரைப்பீர்கள்?",

        // Day 13
        "What are the most pressing environmental challenges in your region, such as plastic waste, air quality, or water scarcity?" to
                "உங்கள் பகுதியில் உள்ள முக்கிய சுற்றுச்சூழல் சவால்கள் யாவை (எ.கா: பிளாஸ்டிக் கழிவுகள், காற்று அல்லது தண்ணீர் பற்றாக்குறை)?",
        "What practical daily actions can individual households take to reduce energy consumption and preserve green spaces?" to
                "மின்சாரப் பயன்பாட்டைக் குறைக்கவும் பசுமையைப் பாதுகாக்கவும் ஒவ்வொரு குடும்பமும் அன்றாடம் செய்யக்கூடிய எளிய செயல்கள் என்ன?",
        "How can schools, youth communities, and local organizations encourage people to plant more trees and recycle responsibly?" to
                "அதிக மரங்களை நடவும், மறுசுழற்சி செய்யவும் பள்ளிகளும் அமைப்புகளும் மக்களை எவ்வாறு ஊக்குவிக்க முடியும்?",

        // Day 14
        "Why is learning to save money, track monthly expenses, and invest wisely crucial for young adults today?" to
                "இன்றைய இளைஞர்கள் பணத்தைச் சேமிக்கவும், மாதாந்திர செலவுகளைக் கண்காணிக்கவும், புத்திசாலித்தனமாக முதலீடு செய்யவும் கற்றுக்கொள்வது ஏன் அவசியம்?",
        "If you received a sudden unexpected cash reward of one lakh rupees today, how would you allocate and spend it?" to
                "இன்று உங்களுக்கு எதிர்பாராதவிதமாக ஒரு லட்சம் ரூபாய் பரிசு கிடைத்தால், அதை எவ்வாறு பிரித்து செலவிடுவீர்கள்?",
        "What is the difference between essential needs and impulsive wants when budgeting your personal finances?" to
                "தனிநபர் நிதித் திட்டமிடலில் அத்தியாவசியத் தேவைகளுக்கும் அவசர ஆசைகளுக்கும் உள்ள வித்தியாசம் என்ன?",

        // Day 15
        "Describe a situation where an energetic team achieved a great result because everyone communicated openly and cooperated." to
                "அனைவரும் வெளிப்படையாக பேசி ஒத்துழைத்ததால் ஒரு குழு சிறந்த வெற்றியை அடைந்த ஒரு சூழ்நிலையை விவரியுங்கள்.",
        "What key leadership qualities are essential when guiding a diverse group of people toward a common goal?" to
                "ஒரு பொதுவான இலக்கை நோக்கி பலதரப்பட்ட மக்களை வழிநடத்தும்போது தேவையான முக்கிய தலைமைப் பண்புகள் என்ன?",
        "How do you handle disagreements or conflicting opinions constructively within a work or study team?" to
                "வேலை அல்லது படிப்பு குழுவில் ஏற்படும் கருத்து வேறுபாடுகளை நீங்கள் எவ்வாறு பயனுள்ள முறையில் கையாளுகிறீர்கள்?",

        // Day 16
        "What makes a speaker captivating, memorable, and persuasive when presenting on stage or in an interview?" to
                "மேடையிலோ அல்லது நேர்காணலிலோ பேசும்போது ஒரு பேச்சாளரை ஈர்க்கக்கூடியவராகவும் மறக்கமுடியாதவராகவும் மாற்றுவது எது?",
        "How do you manage stage fright, nervousness, or racing heartbeats before speaking to a large audience?" to
                "அதிக மக்கள் முன்னிலையில் பேசுவதற்கு முன் ஏற்படும் நடுக்கம் அல்லது பயத்தை நீங்கள் எவ்வாறு சமாளிக்கிறீர்கள்?",
        "Deliver a mini 1-minute speech convincing your audience why daily reading is a superpower for career success." to
                "தினசரி புத்தகம் வாசிப்பது தொழில் வெற்றிக்கு எவ்வளவு பெரிய சக்தி என்பதை விளக்கி 1 நிமிட உரையாற்றுங்கள்.",
        "What polite phrases and listening skills help keep tense discussions calm and productive?" to
                "பதற்றமான விவாதங்களை அமைதியாகவும் பயனுள்ளதாகவும் வைத்திருக்க எந்த பணிவான சொற்களும் கவனிக்கும் திறன்களும் உதவுகின்றன?",

        // Day 17
        "Why is it essential to start saving and investing money early in your career?" to
                "உங்கள் ஆரம்பகால தொழில் வாழ்க்கையிலேயே பணத்தைச் சேமிக்கவும் முதலீடு செய்யவும் தொடங்குவது ஏன் அவசியம்?",
        "What budgeting strategies or spending discipline do you follow to manage monthly expenses?" to
                "மாதாந்திர செலவுகளை நிர்வகிக்க நீங்கள் எந்த பட்ஜெட் முறையை அல்லது செலவு ஒழுக்கத்தைப் பின்பற்றுகிறீர்கள்?",
        "In your view, what is the difference between purchasing for necessity versus impulsive buying?" to
                "உங்கள் பார்வையில், அத்தியாவசிய தேவைக்காக வாங்குவதற்கும் அவசர ஆசையில் வாங்குவதற்கும் என்ன வித்தியாசம்?",
        "Describe your dream modern city or smart town. What futuristic amenities, clean transport, and parks would it have?" to
                "உங்கள் கனவு நவீன நகரம் எப்படி இருக்க வேண்டும்? அதில் என்ன நவீன வசதிகள், தூய போக்குவரத்து மற்றும் பூங்காக்கள் இருக்கும்?",
        "What are the major pros and cons of living in a fast-paced metropolitan city versus a serene countryside village?" to
                "வேகமான பெருநகர வாழ்க்கையையும், அமைதியான கிராமப்புற வாழ்க்கையையும் ஒப்பிடும்போது உள்ள நிறைகுறைகள் என்ன?",
        "How will electric mobility, solar power, and vertical gardening transform future housing communities?" to
                "மின்சார வாகனங்கள், சூரிய சக்தி மற்றும் மாடித் தோட்டங்கள் எதிர்கால வீடுகளை எவ்வாறு மாற்றியமைக்கும்?",

        // Day 18
        "What is the most valuable book, biography, or article you have read recently?" to
                "நீங்கள் சமீபத்தில் படித்த மிகவும் மதிப்புமிக்க புத்தகம், சுயசரிதை அல்லது கட்டுரை எது?",
        "How does maintaining a habit of daily reading expand your vocabulary and analytical thinking?" to
                "தினசரி வாசிக்கும் பழக்கத்தைக் கொண்டிருப்பது உங்கள் சொல்லகராதியையும் பகுப்பாய்வு சிந்தனையையும் எவ்வாறு விரிவுபடுத்துகிறது?",
        "What new topic, language, or philosophy would you love to study deeply this year?" to
                "இந்த ஆண்டு நீங்கள் ஆழமாகப் படிக்க விரும்பும் புதிய தலைப்பு, மொழி அல்லது தத்துவம் எது?",
        "When faced with a difficult life decision, what structured process or trusted people do you consult?" to
                "ஒரு கடினமான வாழ்க்கைத் தீர்மானத்தை எடுக்கும்போது, நீங்கள் எந்த முறையைப் பின்பற்றுகிறீர்கள் அல்லது யாரிடம் ஆலோசனை கேட்கிறீர்கள்?",
        "Share an instance when you had to make a tough choice between short-term comfort and long-term achievement." to
                "தற்காலிக சுகத்திற்கும் நீண்டகால சாதனைக்கும் இடையே நீங்கள் ஒரு கடினமான முடிவை எடுக்க வேண்டியிருந்த தருணத்தைப் பகிருங்கள்.",
        "How do you evaluate both positive and negative consequences before finalizing an important commitment?" to
                "ஒரு முக்கியமான முடிவை எடுப்பதற்கு முன் அதன் சாதக பாதகங்களை நீங்கள் எவ்வாறு கணக்கிடுகிறீர்கள்?",

        // Day 19
        "What foreign culture, festival, or custom has always intrigued you, and why?" to
                "எந்த வெளிநாட்டு கலாச்சாரம், திருவிழா அல்லது வழக்கம் உங்களை எப்போதும் கவர்ந்துள்ளது, ஏன்?",
        "Why is cultural sensitivity and respect crucial in today's interconnected global workplace?" to
                "இன்றைய உலகளாவிய பணியிடத்தில் கலாச்சார உணர்திறனும் மரியாதையும் ஏன் மிகவும் முக்கியமானது?",
        "How do international food, music, and cinema help unite people across different continents?" to
                "சர்வதேச உணவு, இசை மற்றும் சினிமா பல்வேறு கண்டங்களில் உள்ள மக்களை ஒன்றிணைக்க எவ்வாறு உதவுகின்றன?",
        "What is the most colorful and joyful cultural festival celebrated in your region, and how do families celebrate it?" to
                "உங்கள் பகுதியில் கொண்டாடப்படும் மிகவும் வண்ணமயமான திருவிழா எது? குடும்பங்கள் அதை எவ்வாறு கொண்டாடுகின்றன?",
        "Why is it important to celebrate diverse traditional customs while respecting modern inclusive values?" to
                "நவீன கால விழுமியங்களை மதிக்கும் அதே வேளையில் பல்வேறு பாரம்பரிய பழக்கவழக்கங்களைக் கொண்டாடுவது ஏன் முக்கியம்?",
        "Describe your favorite festival attire, delicious sweets, and musical rituals associated with that special day." to
                "அந்த சிறப்பு நாளில் நீங்கள் அணியும் பிடித்த உடை, சுவையான இனிப்புகள் மற்றும் இசை நிகழ்வுகளை விவரியுங்கள்.",

        // Day 20
        "What distinguishes an inspiring leader from a standard manager or supervisor?" to
                "ஒரு உத்வேகம் தரும் தலைவரை சாதாரண மேலாளரிடம் இருந்து வேறுபடுத்துவது எது?",
        "Describe a situation where you had to lead a project, organize an event, or guide team members." to
                "நீங்கள் ஒரு திட்டத்தை வழிநடத்த, ஒரு நிகழ்வை ஏற்பாடு செய்ய அல்லது குழு உறுப்பினர்களை வழிநடத்த வேண்டியிருந்த சூழ்நிலையை விவரியுங்கள்.",
        "How can leaders cultivate high morale, trust, and psychological safety in their teams?" to
                "தலைவர்கள் தங்கள் குழுக்களில் உயர் மன உறுதி, நம்பிக்கை மற்றும் பாதுகாப்பை எவ்வாறு வளர்க்க முடியும்?",
        "Which recent scientific innovation or medical discovery inspires you the most, and what problems does it solve?" to
                "சமீபத்திய எந்த அறிவியல் கண்டுபிடிப்பு அல்லது மருத்துவ முன்னேற்றம் உங்களுக்கு மிகவும் உத்வேகம் அளிக்கிறது, அது என்ன பிரச்சினையை தீர்க்கிறது?",
        "If you could have a 30-minute conversation with any historical inventor or scientist, who would it be and why?" to
                "வரலாற்றில் வாழ்ந்த எந்த விஞ்ஞானியுடனும் 30 நிமிடங்கள் உரையாட முடிந்தால், நீங்கள் யாரைத் தேர்ந்தெடுப்பீர்கள், ஏன்?",
        "How will space exploration and renewable energy research reshape humanity's future over the next century?" to
                "விண்வெளி ஆராய்ச்சியும் புதுப்பிக்கத்தக்க ஆற்றலும் அடுத்த நூற்றாண்டில் மனிதகுலத்தின் எதிர்காலத்தை எவ்வாறு மாற்றும்?",

        // Day 21
        "How has your confidence in speaking English in front of others evolved over the past 21 days?" to
                "கடந்த 21 நாட்களில் மற்றவர்கள் முன் ஆங்கிலத்தில் பேசுவதில் உங்கள் தன்னம்பிக்கை எவ்வாறு உயர்ந்துள்ளது?",
        "What specific grammar structures or vocabulary words have become natural in your spontaneous speech?" to
                "உங்கள் இயல்பான பேச்சில் இப்போது சரளமாக வரக்கூடிய இலக்கண அமைப்புகள் அல்லது புதிய வார்த்தைகள் யாவை?",
        "What is your next ambitious goal for English communication (interviews, public debate, international travels)?" to
                "ஆங்கிலத் தொடர்பாடலில் உங்கள் அடுத்த பெரிய இலக்கு என்ன (நேர்காணல்கள், மேடைப் பேச்சு, வெளிநாட்டுப் பயணம்)?",

        // Practice Topics Questions (All 15 Topics)
        "What is your name and where are you from?" to
                "உங்கள் பெயர் என்ன மற்றும் நீங்கள் எங்கிருந்து வருகிறீர்கள்?",
        "What is your educational or work background?" to
                "உங்கள் கல்வி அல்லது வேலைப் பின்னணி என்ன?",
        "What are your key interests and future goals?" to
                "உங்கள் முக்கிய ஆர்வங்கள் மற்றும் எதிர்கால இலக்குகள் என்ன?",
        "What is the name of your village or city and where is it located?" to
                "உங்கள் கிராமம் அல்லது நகரத்தின் பெயர் என்ன மற்றும் அது எங்கு அமைந்துள்ளது?",
        "What are the most famous places, attractions, or foods there?" to
                "அங்குள்ள மிகவும் பிரபலமான இடங்கள், சுற்றுலாத் தலங்கள் அல்லது உணவுகள் யாவை?",
        "What makes your hometown unique and special to you?" to
                "உங்கள் சொந்த ஊர் உங்களுக்கு ஏன் தனித்துவமாகவும் சிறப்பானதாகவும் இருக்கிறது?",
        "Who is your best friend and how did your friendship begin?" to
                "உங்கள் சிறந்த நண்பர் யார் மற்றும் உங்கள் நட்பு எவ்வாறு தொடங்கியது?",
        "What qualities do you admire most in them?" to
                "அவர்களிடம் நீங்கள் மிகவும் ரசிக்கும் குணங்கள் என்ன?",
        "What is a memorable adventure or funny moment you experienced together?" to
                "நீங்கள் இருவரும் இணைந்து அனுபவித்த மறக்கமுடியாத சாகசம் அல்லது வேடிக்கையான தருணம் என்ன?",
        "What time do you wake up and start your morning?" to
                "நீங்கள் எத்தனை மணிக்கு எழுந்து உங்கள் காலைப் பொழுதைத் தொடங்குகிறீர்கள்?",
        "How do you spend your working or study hours?" to
                "உங்கள் வேலை அல்லது படிப்பு நேரத்தை எவ்வாறு கழிக்கிறீர்கள்?",
        "What is your favorite way to relax before going to sleep?" to
                "தூங்குவதற்கு முன் உங்களுக்கு மிகவும் பிடித்த ஓய்வு எடுக்கும் முறை எது?",
        "What are your favorite hobbies and why do you enjoy them?" to
                "உங்களுக்கு பிடித்த பொழுதுபோக்குகள் என்ன, அவற்றை நீங்கள் ஏன் ரசிக்கிறீர்கள்?",
        "How do you manage time for your hobbies amidst busy routines?" to
                "பரபரப்பான வழக்கங்களுக்கு மத்தியில் உங்கள் பொழுதுபோக்கிற்கான நேரத்தை எவ்வாறு நிர்வகிக்கிறீர்கள்?",
        "Is there a new hobby or creative skill you want to learn?" to
                "நீங்கள் கற்றுக்கொள்ள விரும்பும் புதிய பொழுதுபோக்கு அல்லது ஆக்கப்பூர்வமான திறன் ஏதேனும் உள்ளதா?",
        "What was your favorite subject or memory in school/college?" to
                "பள்ளி அல்லது கல்லூரியில் உங்களுக்கு மிகவும் பிடித்த பாடம் அல்லது நினைவு எது?",
        "Who was an inspiring teacher or professor you remember?" to
                "நீங்கள் நினைவில் வைத்திருக்கும் ஒரு உத்வேகம் தரும் ஆசிரியர் அல்லது பேராசிரியர் யார்?",
        "What is one fun event or cultural fest that you never forget?" to
                "நீங்கள் மறக்க முடியாத ஒரு மகிழ்ச்சியான நிகழ்வு அல்லது கலாச்சார விழா எது?",
        "What is your dream job or profession?" to
                "உங்கள் கனவு வேலை அல்லது தொழில் எது?",
        "What skills and qualifications do you need for this career?" to
                "இந்தத் தொழிலுக்கு என்ன திறன்களும் தகுதிகளும் தேவை?",
        "How do you plan to prepare and reach this milestone?" to
                "இந்த மைல்கல்லை அடைய நீங்கள் எவ்வாறு தயாராக திட்டமிட்டுள்ளீர்கள்?",
        "What do you usually do on weekends?" to
                "நீங்கள் வழக்கமாக வார இறுதி நாட்களில் என்ன செய்வீர்கள்?",
        "What does your ideal Saturday and Sunday look like?" to
                "உங்களின் சிறந்த சனி மற்றும் ஞாயிறு நாட்கள் எவ்வாறு இருக்கும்?",
        "Do you prefer peaceful relaxation or going out with family and friends?" to
                "நீங்கள் அமைதியான ஓய்வை விரும்புகிறீர்களா அல்லது குடும்பம் மற்றும் நண்பர்களுடன் வெளியே செல்ல விரும்புகிறீர்களா?",
        "What is one memorable weekend trip or outing you had recently?" to
                "நீங்கள் சமீபத்தில் சென்ற ஒரு மறக்கமுடியாத வார இறுதி பயணம் அல்லது உல்லாசப் பயணம் எது?",
        "What is your all-time favorite dish and what makes it delicious?" to
                "உங்களுக்கு எப்போதும் பிடித்த உணவு எது மற்றும் அதை சுவையாக மாற்றுவது எது?",
        "Do you prefer home-cooked traditional meals or restaurant food?" to
                "வீட்டில் சமைத்த பாரம்பரிய உணவை விரும்புகிறீர்களா அல்லது உணவக உணவை விரும்புகிறீர்களா?",
        "Can you describe how your favorite food is prepared?" to
                "உங்களுக்கு பிடித்த உணவு எவ்வாறு தயாரிக்கப்படுகிறது என்பதை விவரிக்க முடியுமா?",
        "Where did you travel and who was with you?" to
                "நீங்கள் எங்கு பயணம் செய்தீர்கள் மற்றும் உங்களுடன் யார் இருந்தார்கள்?",
        "What was the most beautiful or exciting experience during the trip?" to
                "பயணத்தின் போது மிகவும் அழகான அல்லது உற்சாகமான அனுபவம் எது?",
        "What did you learn or enjoy most about that journey?" to
                "அந்தப் பயணத்தில் நீங்கள் அதிகம் கற்றுக்கொண்டது அல்லது ரசித்தது என்ன?",
        "Which apps or technological gadgets do you use daily?" to
                "நீங்கள் தினமும் என்ன செயலிகள் அல்லது தொழில்நுட்ப சாதனங்களைப் பயன்படுத்துகிறீர்கள்?",
        "How does technology make your studies, work, or life easier?" to
                "தொழில்நுட்பம் உங்கள் படிப்பு, வேலை அல்லது வாழ்க்கையை எவ்வாறு எளிதாக்குகிறது?",
        "What are some downsides of too much screen time or tech dependency?" to
                "அதிகப்படியான திரை நேரம் அல்லது தொழில்நுட்ப சார்பின் சில தீமைகள் என்ன?",
        "Which festival is your favorite to celebrate?" to
                "கொண்டாட உங்களுக்கு மிகவும் பிடித்த திருவிழா எது?",
        "How do you and your family prepare for and celebrate this day?" to
                "நீங்களும் உங்கள் குடும்பத்தினரும் இந்த நாளுக்கு எவ்வாறு தயாராகி கொண்டாடுகிறீர்கள்?",
        "What is the significance or special message behind this festival?" to
                "இந்த திருவிழாவின் முக்கியத்துவம் அல்லது சிறப்பு செய்தி என்ன?",
        "Why is English important for modern career opportunities?" to
                "நவீன வேலை வாய்ப்புகளுக்கு ஆங்கிலம் ஏன் முக்கியமானது?",
        "How does fluent English boost your self-confidence?" to
                "சரளமாக ஆங்கிலம் பேசுவது உங்கள் தன்னம்பிக்கையை எவ்வாறு உயர்த்துகிறது?",
        "What are the best methods you use to improve your speaking skills?" to
                "உங்கள் பேசும் திறனை மேம்படுத்த நீங்கள் பயன்படுத்தும் சிறந்த வழிகள் யாவை?",
        "Which social media platforms do you use and for what purpose?" to
                "நீங்கள் எந்த சமூக ஊடக தளங்களைப் பயன்படுத்துகிறீர்கள், எதற்காக?",
        "What are the biggest advantages and disadvantages of social media?" to
                "சமூக ஊடகங்களின் மிகப்பெரிய நன்மைகள் மற்றும் தீமைகள் என்ன?",
        "How can people use social media constructively for self-improvement?" to
                "மக்கள் சுய முன்னேற்றத்திற்காக சமூக ஊடகங்களை எவ்வாறு ஆக்கப்பூர்வமாகப் பயன்படுத்தலாம்?",
        "What is an idea, topic, or story you want to share today?" to
                "இன்று நீங்கள் பகிர்ந்து கொள்ள விரும்பும் கருத்து, தலைப்பு அல்லது கதை என்ன?",
        "What inspired you recently?" to
                "சமீபத்தில் உங்களுக்கு உத்வேகம் அளித்தது எது?",
        "Speak continuously for a few minutes about anything you care about." to
                "நீங்கள் விரும்பும் எதைப் பற்றியும் சில நிமிடங்கள் தொடர்ந்து பேசுங்கள்.",
        "What is one thing you learned recently?" to
                "நீங்கள் சமீபத்தில் கற்றுக்கொண்ட ஒரு விஷயம் என்ன?",
        "Tell me more about this." to
                "இதைப் பற்றி மேலும் கூறுங்கள்.",
        "What makes this important to you?" to
                "இது உங்களுக்கு ஏன் முக்கியமானது?",
        "How has this impacted your life or daily routine?" to
                "இது உங்கள் வாழ்க்கையை அல்லது அன்றாட வழக்கத்தை எவ்வாறு பாதித்துள்ளது?"
    )

    /**
     * Returns Tamil translation / explanation for a given English question.
     */
    fun getTamilMeaning(question: String): String {
        val trimmed = question.trim()
        
        // Exact match
        directQuestionMap[trimmed]?.let { return it }

        // Clean punctuation match
        val cleanQ = trimmed.replace("\"", "").replace("'", "").trim()
        directQuestionMap[cleanQ]?.let { return it }

        for ((k, v) in directQuestionMap) {
            val cleanK = k.replace("\"", "").replace("'", "").trim()
            if (cleanK.equals(cleanQ, ignoreCase = true) || cleanQ.contains(cleanK, ignoreCase = true) || cleanK.contains(cleanQ, ignoreCase = true)) {
                return v
            }
        }

        // Smart semantic fallback translation for procedurally generated or custom questions
        return generateDynamicTamilExplanation(cleanQ)
    }

    private fun generateDynamicTamilExplanation(q: String): String {
        val lower = q.lowercase(Locale.ENGLISH)

        return when {
            lower.contains("learned recently") || lower.contains("learn recently") ->
                "நீங்கள் சமீபத்தில் கற்றுக்கொண்ட ஒரு விஷயம் என்ன? அதைப்பற்றி விளக்குங்கள்."

            lower.contains("what is one thing") ->
                "நீங்கள் குறிப்பிட விரும்பும் ஒரு முக்கியமான விஷயம் என்ன? அதைப்பற்றி விவரியுங்கள்."

            lower.contains("what do you usually do on weekends") || (lower.contains("weekends") && lower.contains("usually")) ->
                "நீங்கள் வழக்கமாக வார இறுதி நாட்களில் என்ன செய்வீர்கள்?"

            lower.contains("describe your core perspectives") || (lower.contains("perspectives") && lower.contains("experiences")) ->
                "இத்தலைப்பு தொடர்பான உங்கள் முக்கிய கருத்துக்கள் மற்றும் தனிப்பட்ட அனுபவங்களை விவரியுங்கள்."

            lower.contains("how has your understanding") || lower.contains("evolved over time") ->
                "காலப்போக்கில் இத்தலைப்பு குறித்த உங்கள் புரிதல் எவ்வாறு வளர்ந்துள்ளது? நீங்கள் கற்றுக்கொண்ட முக்கிய பாடம் என்ன?"

            lower.contains("share an ambitious future goal") || lower.contains("thoughtful recommendation") ->
                "இத்தலைப்பு தொடர்பாக ஒரு எதிர்கால இலக்கு அல்லது பயனுள்ள யோசனையைப் பகிருங்கள்."

            lower.contains("introduce yourself") ->
                "உங்களைப் பற்றியும் உங்கள் பின்னணியைப் பற்றியும் தெளிவாக அறிமுகப்படுத்துங்கள்."

            lower.contains("daily routine") || lower.contains("morning routine") ->
                "உங்கள் அன்றாட வழக்கங்கள் மற்றும் நேர நிர்வாகத்தைப் பற்றி விவரியுங்கள்."

            lower.contains("favorite hobby") || lower.contains("free time") ->
                "உங்களுக்கு பிடித்த பொழுதுபோக்கு மற்றும் ஓய்வு நேர செயல்பாடுகளைப் பற்றி பேசுங்கள்."

            lower.contains("dream job") || lower.contains("career") ->
                "உங்கள் கனவு வேலை, தொழில் இலக்குகள் மற்றும் எதிர்காலத் திட்டங்களை விளக்குங்கள்."

            lower.contains("hometown") || lower.contains("city") || lower.contains("village") ->
                "உங்கள் சொந்த ஊர், அதன் சூழல் மற்றும் சிறப்பம்சங்களை விவரியுங்கள்."

            lower.contains("friend") || lower.contains("relationship") ->
                "உங்கள் நண்பர்கள், வழிகாட்டிகள் அல்லது மனித உறவுகளின் முக்கியத்துவத்தைப் பற்றி கூறுங்கள்."

            lower.contains("travel") || lower.contains("trip") || lower.contains("journey") ->
                "உங்கள் பயண அனுபவங்கள் மற்றும் சுற்றுலா நினைவுகளைப் பற்றி பேசுங்கள்."

            lower.contains("food") || lower.contains("cook") || lower.contains("dish") ->
                "உங்களுக்கு பிடித்த உணவுகள் மற்றும் சமையல் அனுபவங்களைப் பற்றி விவரியுங்கள்."

            lower.contains("challenge") || lower.contains("obstacle") ->
                "நீங்கள் சந்தித்த சவால்கள் மற்றும் அதை எவ்வாறு சமாளித்தீர்கள் என்பதை விவரியுங்கள்."

            lower.contains("technology") || lower.contains("ai") ->
                "தொழில்நுட்பம் மற்றும் நவீன கண்டுபிடிப்புகளின் தாக்கம் பற்றி உங்கள் கருத்தைக் கூறுங்கள்."

            lower.startsWith("why") ->
                "இந்தக் கேள்விக்கான காரணத்தை விளக்கி ஆங்கிலத்தில் பேசுங்கள்."

            lower.startsWith("how") ->
                "இது எவ்வாறு செயல்படுகிறது அல்லது நீங்கள் இதை எப்படி செய்கிறீர்கள் என்பதை விளக்குங்கள்."

            lower.startsWith("what") ->
                "கேட்கப்பட்டுள்ள விஷயம் என்ன என்பதைப் பற்றி விரிவாக ஆங்கிலத்தில் பேசுங்கள்."

            lower.startsWith("describe") || lower.startsWith("explain") ->
                "கேட்கப்பட்டுள்ள தலைப்பைப் பற்றி விரிவாக விவரித்து ஆங்கிலத்தில் பதில் கூறுங்கள்."

            else ->
                "இந்தக் கேள்வியைப் புரிந்து கொண்டு, தொடர்புடைய கருத்துக்களை ஆங்கிலத்தில் தெளிவாகப் பேசுங்கள்."
        }
    }
}

