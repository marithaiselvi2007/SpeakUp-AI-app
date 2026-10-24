package com.example.data.repository

import com.example.data.model.DayChallenge
import com.example.data.model.EnglishLevel

object DayChallengeData {
    val dailyChallenges: List<DayChallenge> = listOf(
        DayChallenge(
            dayNumber = 1,
            title = "Day 1: Self Introduction & Background",
            subtitle = "Introduce yourself with confidence, background, and passions",
            category = "Foundations",
            iconName = "Person",
            starterPrompt = "Hi, my name is... I am from... Currently, I am studying/working as... In my free time, I enjoy...",
            questions = listOf(
                "Introduce yourself clearly by stating your full name, where you grew up, and your family background.",
                "Describe your educational background, current job or studies, and what you do on a daily basis.",
                "Explain your primary passions, personal goals, and what you hope to accomplish in the next two years."
            ),
            keywords = listOf(
                "name", "born", "from", "live", "family", "education", "study", "student",
                "college", "school", "degree", "work", "job", "myself", "am", "years", "old",
                "hobby", "goal", "aim", "passionate", "ambition", "introduction", "background"
            ),
            sampleAnswerHints = listOf(
                "Speak 2–3 full sentences: 'My name is... I was born in... and I currently live with my family.'",
                "Mention your field of study or profession and what your core responsibilities are.",
                "Share your personal aspirations, such as mastering English communication or advancing your career."
            )
        ),
        DayChallenge(
            dayNumber = 2,
            title = "Day 2: Hometown, Culture & Landmarks",
            subtitle = "Describe your city, famous spots, and cultural heritage",
            category = "Foundations",
            iconName = "LocationCity",
            starterPrompt = "My hometown is known for... The atmosphere here is... One famous place you must visit is...",
            questions = listOf(
                "What is the name of your hometown or city, and what makes its neighborhood atmosphere unique and lively?",
                "Describe the most famous landmark, historic monument, or scenic spot in your area and why people love visiting it.",
                "Talk about the local traditions, cultural festivals, and delicious regional dishes famous in your hometown."
            ),
            keywords = listOf(
                "hometown", "city", "village", "town", "place", "landmark", "temple", "monument",
                "culture", "tradition", "festival", "food", "people", "atmosphere", "heritage",
                "scenic", "visit", "famous", "located", "state", "district"
            ),
            sampleAnswerHints = listOf(
                "State the geographical location and describe the general vibe or climate.",
                "Paint a verbal picture of a famous park, temple, or historical site in your town.",
                "Describe a festival celebration or unique local cuisine that visitors always enjoy."
            )
        ),
        DayChallenge(
            dayNumber = 3,
            title = "Day 3: Daily Routine & Time Management",
            subtitle = "Master continuous tenses and time sequences",
            category = "Daily Life",
            iconName = "Schedule",
            starterPrompt = "I usually wake up at... Then I prepare for my day by... In the evening, I unwind by...",
            questions = listOf(
                "Describe your typical morning routine from the moment your alarm rings until you leave for work or study.",
                "How do you organize your work or study hours to stay productive and avoid digital distractions?",
                "Explain what you do in the evening to relax, recharge, and prepare yourself for the next day."
            ),
            keywords = listOf(
                "morning", "wake", "alarm", "routine", "breakfast", "schedule", "work", "study",
                "productive", "time", "focus", "evening", "relax", "sleep", "habit", "daily", "exercise"
            ),
            sampleAnswerHints = listOf(
                "Use sequence connectors: 'First, I wake up at 6 AM... Then, I stretch and drink water...'",
                "Discuss your prioritization techniques like making a todo list or taking short breaks.",
                "Describe how you read books, listen to podcasts, or spend quality time with family."
            )
        ),
        DayChallenge(
            dayNumber = 4,
            title = "Day 4: Hobbies, Passions & Leisure",
            subtitle = "Express personal interests with rich vocabulary",
            category = "Daily Life",
            iconName = "SportsSoccer",
            starterPrompt = "One of my favorite hobbies is... It brings me joy because... Whenever I have free time, I...",
            questions = listOf(
                "What is your all-time favorite hobby or creative pastime, and how did you first get interested in it?",
                "How does engaging in this hobby help you relieve stress and develop creative problem-solving skills?",
                "Describe an enjoyable project, match, or creative activity that you participated in recently."
            ),
            keywords = listOf(
                "hobby", "hobbies", "passion", "leisure", "free", "time", "play", "music", "cricket",
                "read", "paint", "draw", "dance", "sing", "cook", "game", "stress", "creative", "enjoy"
            ),
            sampleAnswerHints = listOf(
                "Mention who introduced you to the hobby and how many years you have been practicing it.",
                "Explain the psychological benefits, such as calmness, energy, or sharper focus.",
                "Recount a specific weekend when you spent hours engaged in this activity."
            )
        ),
        DayChallenge(
            dayNumber = 5,
            title = "Day 5: Food, Cuisines & Cooking",
            subtitle = "Talk about flavors, ingredients, and dining experiences",
            category = "Daily Life",
            iconName = "Restaurant",
            starterPrompt = "My favorite dish in the world is... The flavors are... When I cook or eat with family, I like to...",
            questions = listOf(
                "What is your absolute favorite comfort food or traditional dish, and what ingredients make it delicious?",
                "Describe a memorable dining experience at a restaurant or family feast that you still remember fondly.",
                "Do you enjoy cooking? Describe a simple dish you know how to prepare step-by-step."
            ),
            keywords = listOf(
                "food", "dish", "cuisine", "taste", "delicious", "flavor", "spicy", "sweet",
                "cook", "recipe", "restaurant", "meal", "dinner", "lunch", "breakfast", "family", "eat"
            ),
            sampleAnswerHints = listOf(
                "Use sensory adjectives: savory, crispy, fragrant, succulent, spicy.",
                "Describe the ambiance, the people with you, and the standout dishes.",
                "Use cooking verbs: chop, sauté, boil, simmer, season, garnish."
            )
        ),
        DayChallenge(
            dayNumber = 6,
            title = "Day 6: Travel, Vacations & Exploration",
            subtitle = "Describe scenic journeys, nature, and wanderlust",
            category = "Exploration",
            iconName = "Flight",
            starterPrompt = "The most unforgettable trip I took was to... What amazed me the most was... Traveling taught me...",
            questions = listOf(
                "Tell us about an unforgettable travel destination you visited and what made that trip special.",
                "If you could travel anywhere in the world next month with an unlimited budget, where would you go and why?",
                "How does traveling to new places broaden your perspective on people, cultures, and life?"
            ),
            keywords = listOf(
                "travel", "trip", "journey", "vacation", "destination", "mountains", "beach", "scenic",
                "flight", "hotel", "explore", "culture", "nature", "landscape", "adventure", "memory"
            ),
            sampleAnswerHints = listOf(
                "Describe the travel journey, the natural scenery, and the unique activities you did.",
                "Mention famous monuments, international landmarks, or serene nature reserves.",
                "Reflect on how adapting to new environments boosts adaptability and empathy."
            )
        ),
        DayChallenge(
            dayNumber = 7,
            title = "Day 7: Career Ambitions & Future Goals",
            subtitle = "Articulate professional aspirations with power words",
            category = "Professional",
            iconName = "Work",
            starterPrompt = "My dream career is to... In the next 5 years, I aim to... To achieve this milestone, I am currently...",
            questions = listOf(
                "What is your dream job or ideal career role, and what inspired you to pursue this path?",
                "Where do you see yourself professionally five years from now, and what leadership role do you aspire to hold?",
                "What new technical skills or soft skills are you actively learning right now to advance your career?"
            ),
            keywords = listOf(
                "career", "job", "profession", "ambition", "goal", "dream", "success", "future",
                "skills", "leadership", "company", "industry", "promote", "achieve", "milestone", "work"
            ),
            sampleAnswerHints = listOf(
                "Use strong professional verbs: manage, develop, orchestrate, lead, innovate.",
                "Outline specific career progression stages and responsibilities.",
                "Mention communication, technical certifications, and analytical thinking."
            )
        ),
        DayChallenge(
            dayNumber = 8,
            title = "Day 8: Friends, Relationships & Mentors",
            subtitle = "Celebrate human connection, empathy, and loyalty",
            category = "Social",
            iconName = "Group",
            starterPrompt = "A person who inspires me deeply is... What I admire about them is... Our friendship has taught me...",
            questions = listOf(
                "Describe your best friend or closest mentor. What unique qualities do you admire most in them?",
                "Share a story of a time when a friend or family member supported you through a difficult situation.",
                "In your opinion, what are the three most important qualities that make a long-lasting, trustworthy relationship?"
            ),
            keywords = listOf(
                "friend", "friendship", "mentor", "support", "trust", "loyalty", "kindness", "relationship",
                "help", "advice", "inspire", "admire", "care", "honest", "guide", "companion"
            ),
            sampleAnswerHints = listOf(
                "Highlight character traits like patience, generosity, active listening, and integrity.",
                "Narrate the incident using past continuous and simple past tenses.",
                "State mutual respect, transparent communication, and unwavering support."
            )
        ),
        DayChallenge(
            dayNumber = 9,
            title = "Day 9: Technology, AI & Modern Innovation",
            subtitle = "Discuss digital transformation and cutting-edge tools",
            category = "Modern",
            iconName = "Computer",
            starterPrompt = "Technology has revolutionized our lives by... The tech tool I rely on most is... In the future, AI will...",
            questions = listOf(
                "Which modern digital device or mobile application has changed your daily life the most, and how?",
                "What are the biggest benefits and potential challenges of Artificial Intelligence in education and jobs?",
                "How do you strike a healthy balance between using digital gadgets and enjoying offline real-world life?"
            ),
            keywords = listOf(
                "technology", "tech", "computer", "phone", "ai", "artificial", "intelligence", "internet",
                "app", "digital", "device", "software", "innovation", "automation", "future", "screen", "online"
            ),
            sampleAnswerHints = listOf(
                "Discuss instant communication, cloud storage, e-learning platforms, or smart navigation.",
                "Examine productivity gains versus concerns about data privacy and screen fatigue.",
                "Share digital detox habits, such as turning off notifications during study or family time."
            )
        ),
        DayChallenge(
            dayNumber = 10,
            title = "Day 10: Overcoming Challenges & Resilience",
            subtitle = "Narrate adversity, problem-solving, and personal grit",
            category = "Personal Growth",
            iconName = "TrendingUp",
            starterPrompt = "A challenging moment in my life occurred when... I managed to overcome it by... That experience taught me...",
            questions = listOf(
                "Tell a story about an unexpected obstacle or difficult problem you faced in school, college, or work.",
                "What mindset, steps, or help from others enabled you to overcome that challenge successfully?",
                "What did that difficult experience teach you about resilience, mental courage, and self-confidence?"
            ),
            keywords = listOf(
                "challenge", "obstacle", "problem", "overcome", "solution", "difficult", "hard",
                "resilience", "courage", "learn", "lesson", "experience", "strong", "growth", "succeed"
            ),
            sampleAnswerHints = listOf(
                "Set the scene clearly: 'Last year during final exams, I faced a major setback when...'",
                "Break down the action steps: analyzing the issue, staying calm, and seeking solutions.",
                "Reflect on how overcoming struggles builds emotional resilience and future readiness."
            )
        ),
        DayChallenge(
            dayNumber = 11,
            title = "Day 11: Movies, Music & Storytelling",
            subtitle = "Critique art, cinema, melodies, and storytelling",
            category = "Entertainment",
            iconName = "Movie",
            starterPrompt = "The most impactful movie/song I know is... The plot and message revolve around... It resonates with me because...",
            questions = listOf(
                "What is your all-time favorite movie, book, or TV series, and what is its core moral message?",
                "How does music or cinema influence your emotions, imagination, and day-to-day motivation?",
                "If you were a film director, what compelling story or social issue would you want to create a film about?"
            ),
            keywords = listOf(
                "movie", "film", "cinema", "music", "song", "story", "character", "actor",
                "plot", "theme", "message", "director", "book", "entertainment", "emotion", "inspire"
            ),
            sampleAnswerHints = listOf(
                "Summarize the storyline succinctly without spoiling major twists.",
                "Describe acoustic instruments, meaningful lyrical lines, or cinematic cinematography.",
                "Discuss social empowerment, educational inequality, or uplifting real-life biographies."
            )
        ),
        DayChallenge(
            dayNumber = 12,
            title = "Day 12: Public Speaking & Pitching Ideas",
            subtitle = "Persuade, convince, and present with authority",
            category = "Professional",
            iconName = "RecordVoiceOver",
            starterPrompt = "If I had two minutes to pitch an innovative idea to investors, I would say... The core problem we solve is...",
            questions = listOf(
                "Imagine you have 60 seconds to pitch a smart business or community idea. What problem does it solve?",
                "What public speaking techniques (body language, eye contact, vocal modulation) do you find most effective?",
                "How do you handle nervousness, stage fear, or sudden memory blanks during formal presentations?"
            ),
            keywords = listOf(
                "speech", "speak", "pitch", "presentation", "audience", "idea", "innovate", "convince",
                "persuade", "stage", "voice", "confidence", "public", "eye", "contact", "modulate"
            ),
            sampleAnswerHints = listOf(
                "Start with a strong hook: 'Have you ever noticed that millions of students struggle to...'",
                "Highlight pause power, varying pitch, and projecting the voice clearly.",
                "Discuss deep breathing, structured notes, and focusing on delivering value to the audience."
            )
        ),
        DayChallenge(
            dayNumber = 13,
            title = "Day 13: Environment, Nature & Sustainability",
            subtitle = "Advocate for climate care, green energy, and ecology",
            category = "Global Issues",
            iconName = "Eco",
            starterPrompt = "Protecting our planet is vital because... One eco-friendly habit I practice is... Communities should...",
            questions = listOf(
                "What environmental challenge (such as plastic pollution or deforestation) concerns you most, and why?",
                "What simple eco-friendly lifestyle changes can individuals make every day to reduce waste and save energy?",
                "How can schools and local city leaders inspire the younger generation to plant trees and protect nature?"
            ),
            keywords = listOf(
                "environment", "nature", "planet", "earth", "climate", "pollution", "green", "recycle",
                "trees", "forest", "clean", "sustainable", "eco", "waste", "water", "solar", "protect"
            ),
            sampleAnswerHints = listOf(
                "Identify single-use plastics, air pollution, or water scarcity.",
                "Mention reusable bags, conserving electricity, cycling, and segregating wet and dry waste.",
                "Suggest community tree-planting drives, recycling competitions, and outdoor workshops."
            )
        ),
        DayChallenge(
            dayNumber = 14,
            title = "Day 14: Health, Fitness & Mental Wellbeing",
            subtitle = "Express habits for stamina, mindfulness, and nutrition",
            category = "Wellbeing",
            iconName = "Favorite",
            starterPrompt = "To keep myself healthy, I regularly... A morning walk or exercise helps me by... I also try to...",
            questions = listOf(
                "What physical activities, sports, or morning exercises do you do every week to stay energetic and fit?",
                "How does maintaining a regular fitness routine impact your mental clarity, mood, and daily productivity?",
                "What healthy dietary habits, hydration routines, or sleep schedules are you planning to practice this month?"
            ),
            keywords = listOf(
                "health", "fitness", "exercise", "walk", "workout", "diet", "nutrition", "sleep",
                "wellbeing", "mental", "energy", "active", "body", "mind", "routine", "healthy"
            ),
            sampleAnswerHints = listOf(
                "Mention jogging, yoga, gym workouts, or playing badminton in the evening.",
                "Explain how exercise releases endorphins and reduces stress after intense work.",
                "Discuss drinking sufficient water, eating green vegetables, and sleeping 7–8 hours."
            )
        ),
        DayChallenge(
            dayNumber = 15,
            title = "Day 15: The Power of English Fluency",
            subtitle = "Reflect on language mastery, confidence, and global opportunities",
            category = "Milestone",
            iconName = "EmojiEvents",
            starterPrompt = "English fluency is important for me because... When I first started speaking, I felt... Now I...",
            questions = listOf(
                "Why is English communication essential for your personal self-confidence and career advancement?",
                "What hesitations or pronunciation fears did you experience earlier, and how has daily practice helped you?",
                "Share three practical speaking strategies you will practice consistently to become a charismatic, fluent speaker."
            ),
            keywords = listOf(
                "english", "fluency", "fluent", "speak", "practice", "confidence", "communication",
                "vocabulary", "grammar", "pronunciation", "global", "career", "improve", "language", "voice"
            ),
            sampleAnswerHints = listOf(
                "Highlight global job opportunities, attending international interviews, and expressive conversations.",
                "Share how speaking aloud daily eliminated the fear of making grammatical errors.",
                "List thinking in English, recording daily voice notes, and expanding vocabulary through reading."
            )
        ),
        DayChallenge(
            dayNumber = 16,
            title = "Day 16: Effective Negotiation & Problem Solving",
            subtitle = "Resolve disagreements diplomatically with courteous language",
            category = "Professional",
            iconName = "Handshake",
            starterPrompt = "In any difficult negotiation, my approach is... When two parties disagree, it is important to...",
            questions = listOf(
                "How do you handle disagreements with colleagues or friends without damaging the relationship?",
                "Describe a situation where finding a middle ground or compromise created a win-win result.",
                "What polite phrases and listening skills help keep tense discussions calm and productive?"
            ),
            keywords = listOf(
                "negotiate", "compromise", "agreement", "disagree", "polite", "diplomatic", "solution",
                "listen", "respect", "perspective", "win", "calm", "professional", "discussion"
            ),
            sampleAnswerHints = listOf(
                "Use diplomatic phrases: 'I see your point, however...', 'Could we consider...'",
                "Focus on shared objectives rather than personal clashes.",
                "Emphasize empathetic listening and acknowledging valid points."
            )
        ),
        DayChallenge(
            dayNumber = 17,
            title = "Day 17: Financial Wisdom & Money Management",
            subtitle = "Discuss budgeting, investments, and economic smartness",
            category = "Daily Life",
            iconName = "AccountBalance",
            starterPrompt = "Managing money wisely requires... One smart financial habit everyone should build is...",
            questions = listOf(
                "Why is it essential to start saving and investing money early in your career?",
                "What budgeting strategies or spending discipline do you follow to manage monthly expenses?",
                "In your view, what is the difference between purchasing for necessity versus impulsive buying?"
            ),
            keywords = listOf(
                "money", "finance", "budget", "save", "invest", "expense", "income", "wealth",
                "economy", "discipline", "bank", "future", "financial", "smart", "spending"
            ),
            sampleAnswerHints = listOf(
                "Discuss emergency funds, compound interest, and future security.",
                "Mention tracking daily expenses using digital sheets or budgeting apps.",
                "Explain the 50/30/20 rule or waiting 24 hours before making major non-essential purchases."
            )
        ),
        DayChallenge(
            dayNumber = 18,
            title = "Day 18: Books, Wisdom & Lifelong Learning",
            subtitle = "Share insights from literature, biographies, and articles",
            category = "Education",
            iconName = "MenuBook",
            starterPrompt = "A book or article that changed my perspective is... The most powerful takeaway was...",
            questions = listOf(
                "What is the most valuable book, biography, or article you have read recently?",
                "How does maintaining a habit of daily reading expand your vocabulary and analytical thinking?",
                "What new topic, language, or philosophy would you love to study deeply this year?"
            ),
            keywords = listOf(
                "book", "read", "author", "lesson", "knowledge", "learn", "wisdom", "chapter",
                "biography", "perspective", "thinking", "intellect", "habit", "study", "expand"
            ),
            sampleAnswerHints = listOf(
                "Mention key concepts, unforgettable quotes, and character decisions.",
                "Explain cognitive benefits like improved concentration and linguistic richness.",
                "Discuss topics like psychology, history, astronomy, or artificial intelligence."
            )
        ),
        DayChallenge(
            dayNumber = 19,
            title = "Day 19: Cultural Diversity & World Heritage",
            subtitle = "Celebrate diverse global traditions and linguistic harmony",
            category = "Global Issues",
            iconName = "Public",
            starterPrompt = "Learning about different cultures fascinates me because... A foreign tradition I admire is...",
            questions = listOf(
                "What foreign culture, festival, or custom has always intrigued you, and why?",
                "Why is cultural sensitivity and respect crucial in today's interconnected global workplace?",
                "How do international food, music, and cinema help unite people across different continents?"
            ),
            keywords = listOf(
                "culture", "world", "diversity", "tradition", "global", "heritage", "respect",
                "international", "custom", "festival", "harmony", "unity", "languages", "people"
            ),
            sampleAnswerHints = listOf(
                "Mention Japanese hospitality, Latin American carnivals, or European art festivals.",
                "Explain how cross-cultural awareness prevents misunderstandings in remote teams.",
                "Reflect on how shared arts transcend geographical boundaries."
            )
        ),
        DayChallenge(
            dayNumber = 20,
            title = "Day 20: Leadership & Inspiring Others",
            subtitle = "Demonstrate vision, integrity, and team motivation",
            category = "Professional",
            iconName = "MilitaryTech",
            starterPrompt = "A true leader is not someone who gives orders, but someone who... Great leaders inspire by...",
            questions = listOf(
                "What distinguishes an inspiring leader from a standard manager or supervisor?",
                "Describe a situation where you had to lead a project, organize an event, or guide team members.",
                "How can leaders cultivate high morale, trust, and psychological safety in their teams?"
            ),
            keywords = listOf(
                "leader", "leadership", "inspire", "vision", "team", "guide", "integrity",
                "motivate", "trust", "responsibility", "example", "organize", "empower", "growth"
            ),
            sampleAnswerHints = listOf(
                "Contrast empathy, vision, and lead-by-example with authoritarian supervision.",
                "Recount organizing college cultural fests, team hackathons, or family events.",
                "Highlight constructive feedback, celebrating team wins, and open dialogue."
            )
        ),
        DayChallenge(
            dayNumber = 21,
            title = "Day 21: Three-Week Fluency Mastery & Reflection",
            subtitle = "Review massive spoken English milestones and rapid progress",
            category = "Milestone",
            iconName = "WorkspacePremium",
            starterPrompt = "Completing 21 continuous days of speaking practice has transformed my... I now feel...",
            questions = listOf(
                "How has your confidence in speaking English in front of others evolved over the past 21 days?",
                "What specific grammar structures or vocabulary words have become natural in your spontaneous speech?",
                "What is your next ambitious goal for English communication (interviews, public debate, international travels)?"
            ),
            keywords = listOf(
                "milestone", "twenty", "days", "streak", "transform", "progress", "confidence",
                "fluency", "mastery", "achievement", "spontaneous", "grammar", "vocabulary", "goal"
            ),
            sampleAnswerHints = listOf(
                "Celebrate overcoming hesitation and speaking without mental translation.",
                "Highlight active use of transitions like 'furthermore', 'on the other hand', 'consequently'.",
                "Set concrete targets like acing job interviews or leading international webinars."
            )
        )
    )

    /**
     * Retrieves or procedurally generates an adaptive speaking challenge for ANY day number (Day 1..1000+).
     * Strictly tailors questions, starter prompts, and guidance to the user's actual English level:
     * - BEGINNER: Simple, short, easy-to-understand questions. No confusing or complex wording.
     * - INTERMEDIATE: Conversational questions asking for reasons and smooth connectors.
     * - ADVANCED: In-depth analytical and reflective questions for fluent monologues.
     */
    fun getChallengeForDay(
        dayNumber: Int,
        level: EnglishLevel = EnglishLevel.BEGINNER,
        focusArea: String? = null
    ): DayChallenge {
        // Return curated challenge adapted to level if within curated set
        val curated = dailyChallenges.firstOrNull { it.dayNumber == dayNumber }
        if (curated != null && focusArea.isNullOrBlank()) {
            val cleanTitle = curated.title.substringAfter(": ")
            val adaptiveSubtitle = when (level) {
                EnglishLevel.BEGINNER -> "Simple and clear speaking practice. Speak slowly using easy sentences."
                EnglishLevel.INTERMEDIATE -> "${curated.subtitle}. Connect your ideas smoothly with transition words."
                EnglishLevel.ADVANCED -> "${curated.subtitle}. Deliver an eloquent, structured monologue."
            }

            val adaptiveQuestions = when (level) {
                EnglishLevel.BEGINNER -> getBeginnerQuestions(dayNumber, cleanTitle, curated.questions)
                EnglishLevel.INTERMEDIATE -> getIntermediateQuestions(dayNumber, cleanTitle, curated.questions)
                EnglishLevel.ADVANCED -> curated.questions
            }

            val adaptiveStarterPrompt = when (level) {
                EnglishLevel.BEGINNER -> getBeginnerStarterPrompt(dayNumber, cleanTitle, curated.starterPrompt)
                EnglishLevel.INTERMEDIATE -> curated.starterPrompt
                EnglishLevel.ADVANCED -> "Reflecting on $cleanTitle, it is essential to emphasize that..."
            }

            val adaptiveHints = when (level) {
                EnglishLevel.BEGINNER -> listOf(
                    "Speak 1 or 2 simple sentences at a time.",
                    "Use easy words like 'I am', 'I like', and 'Because'.",
                    "Take your time and speak slowly and clearly."
                )
                EnglishLevel.INTERMEDIATE -> curated.sampleAnswerHints
                EnglishLevel.ADVANCED -> listOf(
                    "Open with a strong, thesis-driven introductory statement.",
                    "Integrate discourse markers like 'Furthermore', 'Consequently', and 'In contrast'.",
                    "Conclude with an insightful forward-looking takeaway."
                )
            }

            return curated.copy(
                title = "Day $dayNumber: $cleanTitle",
                subtitle = adaptiveSubtitle,
                questions = adaptiveQuestions,
                starterPrompt = adaptiveStarterPrompt,
                sampleAnswerHints = adaptiveHints
            )
        }

        // Procedural generation for days beyond curated set or custom adaptive challenges
        val baseTopics = listOf(
            Triple("Self Expression & Ambitions", "Foundations", "Person"),
            Triple("Hometown & Cultural Identity", "Culture", "LocationCity"),
            Triple("Daily Life & Habit Systems", "Lifestyle", "Schedule"),
            Triple("Passions & Creative Arts", "Creative", "Palette"),
            Triple("Memorable Journeys & Wanderlust", "Travel", "FlightTakeoff"),
            Triple("Culinary Delights & Food Heritage", "Food", "Restaurant"),
            Triple("Cinema, Storytelling & Literature", "Media", "Movie"),
            Triple("Technological Innovations & Future", "Tech", "Devices"),
            Triple("Health, Fitness & Mindset", "Wellness", "FitnessCenter"),
            Triple("Global Issues & Eco Sustainability", "Society", "Public"),
            Triple("Career Growth & Leadership", "Professional", "Work"),
            Triple("Friendship, Empathy & Relationships", "Social", "Diversity3"),
            Triple("Money, Financial Wisdom & Value", "Finance", "Payments"),
            Triple("Public Speaking & Persuasive Logic", "Communication", "RecordVoiceOver"),
            Triple("Milestone Reflection & Fluency Growth", "Mastery", "WorkspacePremium"),
            Triple("Overcoming Difficult Challenges", "Resilience", "EmojiEvents"),
            Triple("Future Cities & Modern Living", "Modern", "Apartment"),
            Triple("Decision Making & Critical Thinking", "Intellect", "Lightbulb"),
            Triple("Cultural Festivals & Global Traditions", "Celebration", "Celebration"),
            Triple("Scientific Discoveries & Human Curiosity", "Science", "Psychology")
        )

        val topicIndex = (dayNumber - 1) % baseTopics.size
        val (topicTitle, topicCategory, topicIcon) = baseTopics[topicIndex]

        val levelPrompt = when (level) {
            EnglishLevel.BEGINNER -> "Focus on clear pronunciation and simple sentences."
            EnglishLevel.INTERMEDIATE -> "Use varied connectors like 'Furthermore', 'However', and 'In addition'."
            EnglishLevel.ADVANCED -> "Deliver an eloquent monologue with rhetorical flow and idiomatic precision."
        }

        val focusSuffix = if (!focusArea.isNullOrBlank()) " Focus on: $focusArea." else ""

        val proceduralQuestions = when (level) {
            EnglishLevel.BEGINNER -> listOf(
                "What is one simple thing you like about $topicTitle?",
                "Can you tell me about your daily experience with $topicTitle?",
                "What is one easy goal you have for $topicTitle?"
            )
            EnglishLevel.INTERMEDIATE -> listOf(
                "Describe your thoughts and experiences regarding $topicTitle.",
                "Why is $topicTitle important in your daily life or studies?",
                "What is a positive change or future goal you want to achieve with $topicTitle?"
            )
            EnglishLevel.ADVANCED -> listOf(
                "Critically analyze your core perspectives and formative experiences regarding $topicTitle.",
                "How has your understanding of $topicTitle evolved over time, and what broader lessons stand out?",
                "Share an ambitious future goal or thoughtful recommendation related to $topicTitle."
            )
        }

        val proceduralStarter = when (level) {
            EnglishLevel.BEGINNER -> "Today on Day $dayNumber, I want to talk about $topicTitle. I think that..."
            EnglishLevel.INTERMEDIATE -> "Today on Day $dayNumber, I would like to talk about $topicTitle because..."
            EnglishLevel.ADVANCED -> "Reflecting deeply on $topicTitle today, one realizes that..."
        }

        val proceduralHints = when (level) {
            EnglishLevel.BEGINNER -> listOf(
                "Use short, simple sentences.",
                "Speak slowly so every word is clear.",
                "You can say: 'I like this because it is fun and useful.'"
            )
            EnglishLevel.INTERMEDIATE -> listOf(
                "Open with a clear introductory sentence.",
                "Provide a real-life example or personal story.",
                "Conclude with a forward-looking reflection or piece of advice."
            )
            EnglishLevel.ADVANCED -> listOf(
                "Structure your thoughts with a clear premise, supporting arguments, and synthesis.",
                "Use precise idiomatic phrasing and sophisticated transitions.",
                "Demonstrate vocal modulation and confident rhetorical pacing."
            )
        }

        return DayChallenge(
            dayNumber = dayNumber,
            title = "Day $dayNumber: $topicTitle",
            subtitle = "Day $dayNumber Speaking Challenge. $levelPrompt$focusSuffix",
            category = topicCategory,
            iconName = topicIcon,
            starterPrompt = proceduralStarter,
            questions = proceduralQuestions,
            keywords = listOf(
                topicTitle.lowercase().split(" ").firstOrNull() ?: "experience",
                "perspective", "growth", "communication", "fluency", "confidence",
                "progress", "challenge", "develop", "express", "future", "insight"
            ),
            sampleAnswerHints = proceduralHints
        )
    }

    private fun getBeginnerQuestions(dayNumber: Int, topicTitle: String, defaultQuestions: List<String>): List<String> {
        return when (dayNumber) {
            1 -> listOf(
                "What is your name and where do you live?",
                "What do you do every day (study or work)?",
                "What is one thing you like to do in your free time?"
            )
            2 -> listOf(
                "What is the name of your hometown or city?",
                "What is one famous or beautiful place there?",
                "What special food or festival do you like in your town?"
            )
            3 -> listOf(
                "What time do you wake up in the morning?",
                "What do you do during the afternoon?",
                "How do you relax in the evening before going to bed?"
            )
            4 -> listOf(
                "What is your favorite hobby or sport?",
                "When do you find time to enjoy your hobby?",
                "Why does this activity make you happy?"
            )
            5 -> listOf(
                "What is your favorite food or dish?",
                "Do you like home food or restaurant food more, and why?",
                "Can you name two simple ingredients in your favorite food?"
            )
            else -> listOf(
                "Tell me one thing you know about $topicTitle.",
                "Do you like $topicTitle? Why or why not?",
                "What is one simple sentence you can say about this?"
            )
        }
    }

    private fun getIntermediateQuestions(dayNumber: Int, topicTitle: String, defaultQuestions: List<String>): List<String> {
        return when (dayNumber) {
            1 -> listOf(
                "Introduce yourself by sharing your background, studies or job, and where you grew up.",
                "What are two important responsibilities or skills you focus on daily?",
                "Where do you see yourself professionally in the next two years?"
            )
            2 -> listOf(
                "Describe your hometown and what makes its neighborhood atmosphere pleasant.",
                "What is a famous landmark in your city that visitors should see?",
                "Tell me about a special local dish or festival unique to your region."
            )
            3 -> listOf(
                "Walk through your typical morning and how you prepare for a productive day.",
                "How do you manage your time when you have multiple tasks to finish?",
                "What is your favorite way to unwind and recharge after a busy day?"
            )
            else -> defaultQuestions
        }
    }

    private fun getBeginnerStarterPrompt(dayNumber: Int, topicTitle: String, defaultPrompt: String): String {
        return when (dayNumber) {
            1 -> "Hello! My name is... I live in... In my free time, I like to..."
            2 -> "My hometown is... It is a very nice place because..."
            3 -> "I usually wake up at... Then I... In the evening, I like to..."
            4 -> "My favorite hobby is... I enjoy it because it makes me feel..."
            5 -> "My favorite food to eat is... It tastes delicious because..."
            else -> "Today I want to talk about $topicTitle. I think that..."
        }
    }
}
