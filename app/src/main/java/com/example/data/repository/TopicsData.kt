package com.example.data.repository

import com.example.data.model.PracticeTopic

object TopicsData {
    val allTopics: List<PracticeTopic> = listOf(
        PracticeTopic(
            id = "self_intro",
            title = "Self Introduction",
            subtitle = "Introduce yourself, your background, family, education, and aspirations",
            category = "Basics",
            iconName = "Person",
            starterPrompt = "Introduce yourself clearly and confidently in English. Speak about your name, where you come from, your education, interests, and personal goals.",
            sampleQuestions = listOf(
                "What is your name and where are you from?",
                "What is your educational or work background?",
                "What are your key interests and future goals?"
            )
        ),
        PracticeTopic(
            id = "my_village_city",
            title = "My Village / City",
            subtitle = "Describe your hometown, famous landmarks, culture, food, and people",
            category = "Places",
            iconName = "LocationCity",
            starterPrompt = "Talk about your hometown, city, or village. Describe the atmosphere, popular places, local culture, climate, and why you love it.",
            sampleQuestions = listOf(
                "What is the name of your village or city and where is it located?",
                "What are the most famous places, attractions, or foods there?",
                "What makes your hometown unique and special to you?"
            )
        ),
        PracticeTopic(
            id = "my_best_friend",
            title = "My Best Friend",
            subtitle = "Talk about your best friend, how you met, their qualities, and shared memories",
            category = "Social",
            iconName = "People",
            starterPrompt = "Speak about your best friend. Share how you met, their personality, fun memories you shared, and why your bond is strong.",
            sampleQuestions = listOf(
                "Who is your best friend and how did your friendship begin?",
                "What qualities do you admire most in them?",
                "What is a memorable adventure or funny moment you experienced together?"
            )
        ),
        PracticeTopic(
            id = "daily_routine",
            title = "Daily Routine",
            subtitle = "Describe your morning schedule, daily work/studies, and evening relaxation",
            category = "Everyday",
            iconName = "Schedule",
            starterPrompt = "Walk through a typical day in your life from morning to night. Describe your morning habits, productivity hours, meals, and how you wind down.",
            sampleQuestions = listOf(
                "What time do you wake up and start your morning?",
                "How do you spend your working or study hours?",
                "What is your favorite way to relax before going to sleep?"
            )
        ),
        PracticeTopic(
            id = "hobbies_free_time",
            title = "Hobbies and Free Time",
            subtitle = "Share your favorite hobbies, sports, creative passions, and weekend pastimes",
            category = "Interests",
            iconName = "SportsEsports",
            starterPrompt = "Speak about what you love doing in your free time. Explain your hobbies, when you started them, and how they help you refresh your mind.",
            sampleQuestions = listOf(
                "What are your favorite hobbies and why do you enjoy them?",
                "How do you manage time for your hobbies amidst busy routines?",
                "Is there a new hobby or creative skill you want to learn?"
            )
        ),
        PracticeTopic(
            id = "college_school_days",
            title = "College / School Days",
            subtitle = "Share cherished memories from school, college, teachers, and student life",
            category = "Education",
            iconName = "School",
            starterPrompt = "Talk about your school or college days. Share memories of your favorite teachers, friends, subjects, campus events, and lessons you learned.",
            sampleQuestions = listOf(
                "What was your favorite subject or memory in school/college?",
                "Who was an inspiring teacher or professor you remember?",
                "What is one fun event or cultural fest that you never forget?"
            )
        ),
        PracticeTopic(
            id = "dream_job",
            title = "Dream Job",
            subtitle = "Describe your dream career, role, company, skills needed, and ambitions",
            category = "Career",
            iconName = "Work",
            starterPrompt = "Describe your dream job or ideal career. Explain what role you desire, why you are passionate about it, and how you plan to achieve it.",
            sampleQuestions = listOf(
                "What is your dream job or profession?",
                "What skills and qualifications do you need for this career?",
                "How do you plan to prepare and reach this milestone?"
            )
        ),
        PracticeTopic(
            id = "weekend_activities",
            title = "Weekend Activities",
            subtitle = "How you spend Saturdays and Sundays, family outings, and unwinding",
            category = "Lifestyle",
            iconName = "Weekend",
            starterPrompt = "Talk about how you spend your weekends. Describe your favorite weekend activities, whether you prefer staying home, going on trips, or meeting friends.",
            sampleQuestions = listOf(
                "What does your ideal Saturday and Sunday look like?",
                "Do you prefer peaceful relaxation or going out with family and friends?",
                "What is one memorable weekend trip or outing you had recently?"
            )
        ),
        PracticeTopic(
            id = "favourite_food",
            title = "Favourite Food",
            subtitle = "Describe your favorite dishes, home-cooked food, treats, and recipes",
            category = "Lifestyle",
            iconName = "Restaurant",
            starterPrompt = "Talk about your favorite food, traditional meals, snacks, or cuisines. Describe the taste, aroma, and whether you enjoy cooking or eating out.",
            sampleQuestions = listOf(
                "What is your all-time favorite dish and what makes it delicious?",
                "Do you prefer home-cooked traditional meals or restaurant food?",
                "Can you describe how your favorite food is prepared?"
            )
        ),
        PracticeTopic(
            id = "memorable_journey",
            title = "Memorable Journey",
            subtitle = "Narrate an exciting travel experience, vacation, road trip, or pilgrimage",
            category = "Travel",
            iconName = "Flight",
            starterPrompt = "Narrate a memorable journey or trip you took. Describe where you went, how you travelled, the scenery, food, and the most unforgettable moments.",
            sampleQuestions = listOf(
                "Where did you travel and who was with you?",
                "What was the most beautiful or exciting experience during the trip?",
                "What did you learn or enjoy most about that journey?"
            )
        ),
        PracticeTopic(
            id = "technology_daily_life",
            title = "Technology in Daily Life",
            subtitle = "How smartphones, AI, internet, and gadgets shape your everyday world",
            category = "Modern",
            iconName = "Devices",
            starterPrompt = "Speak about how technology influences your everyday life. Discuss smartphones, AI tools, online learning, and the benefits or challenges of technology.",
            sampleQuestions = listOf(
                "Which apps or technological gadgets do you use daily?",
                "How does technology make your studies, work, or life easier?",
                "What are some downsides of too much screen time or tech dependency?"
            )
        ),
        PracticeTopic(
            id = "festival_you_celebrate",
            title = "A Festival You Celebrate",
            subtitle = "Describe special traditions, festive foods, clothes, celebrations, and joy",
            category = "Culture",
            iconName = "Celebration",
            starterPrompt = "Talk about your favorite festival. Describe how it is celebrated in your family, the rituals, special sweets, traditional clothes, and festive excitement.",
            sampleQuestions = listOf(
                "Which festival is your favorite to celebrate?",
                "How do you and your family prepare for and celebrate this day?",
                "What is the significance or special message behind this festival?"
            )
        ),
        PracticeTopic(
            id = "importance_learning_english",
            title = "Importance of Learning English",
            subtitle = "Why English matters for global career, confidence, travel, and communication",
            category = "Education",
            iconName = "Language",
            starterPrompt = "Speak about why learning English is important for you and for learners worldwide. Discuss its role in career growth, global communication, and personal confidence.",
            sampleQuestions = listOf(
                "Why is English important for modern career opportunities?",
                "How does fluent English boost your self-confidence?",
                "What are the best methods you use to improve your speaking skills?"
            )
        ),
        PracticeTopic(
            id = "social_media",
            title = "Social Media",
            subtitle = "Pros and cons of social platforms, staying connected, and digital habits",
            category = "Modern",
            iconName = "Share",
            starterPrompt = "Express your thoughts on social media. Discuss your favorite platforms, how you use them to learn or stay connected, and how to maintain healthy digital balance.",
            sampleQuestions = listOf(
                "Which social media platforms do you use and for what purpose?",
                "What are the biggest advantages and disadvantages of social media?",
                "How can people use social media constructively for self-improvement?"
            )
        ),
        PracticeTopic(
            id = "open_topic",
            title = "Open Topic",
            subtitle = "Speak freely on any subject, thought, passion, story, or opinion of your choice",
            category = "Open",
            iconName = "Chat",
            starterPrompt = "Express your thoughts freely in English on any subject on your mind. Speak continuously with confidence on your ideas, opinions, experiences, or dreams.",
            sampleQuestions = listOf(
                "What is an idea, topic, or story you want to share today?",
                "What inspired you recently?",
                "Speak continuously for a few minutes about anything you care about."
            )
        ),
        PracticeTopic(
            id = "job_interview_prep",
            title = "Job Interview Preparation",
            subtitle = "Practice classic interview questions, career strengths, and workplace value",
            category = "Professional",
            iconName = "Work",
            starterPrompt = "Good morning, thank you for this opportunity. I would like to highlight my strengths, technical skills, and career passion...",
            sampleQuestions = listOf(
                "Why are you the ideal candidate for this position?",
                "Tell me about a difficult challenge you solved at work or in college.",
                "Where do you see your professional career in the next five years?"
            )
        ),
        PracticeTopic(
            id = "airport_and_travel",
            title = "At the Airport & Traveling Abroad",
            subtitle = "Flight check-in, baggage, boarding gate questions, and hotel check-in",
            category = "Travel",
            iconName = "Flight",
            starterPrompt = "Excuse me, could you please direct me to boarding gate B12? I would like to check in my luggage...",
            sampleQuestions = listOf(
                "How do you interact with airline staff at the check-in desk?",
                "Describe how you would ask for directions when lost in an international airport.",
                "How do you book a hotel room and confirm arrival details in English?"
            )
        ),
        PracticeTopic(
            id = "dining_restaurant",
            title = "Dining Out & Ordering Food",
            subtitle = "Ordering delicious meals, asking about ingredients, and paying the bill",
            category = "Everyday",
            iconName = "Restaurant",
            starterPrompt = "Hello, could we get a table for two by the window please? What would you recommend for today's special?",
            sampleQuestions = listOf(
                "How do you politely order your favorite meal and beverage at a restaurant?",
                "What would you say if your food order was incorrect or delayed?",
                "How do you ask for the bill and compliment the chef or staff?"
            )
        ),
        PracticeTopic(
            id = "shopping_bargaining",
            title = "Shopping & Bargaining",
            subtitle = "Inquiring about sizes, comparing prices, and polite negotiation",
            category = "Lifestyle",
            iconName = "Payments",
            starterPrompt = "Excuse me, do you have this shirt in a medium size? Could you check if there is any ongoing discount?",
            sampleQuestions = listOf(
                "How do you ask a shopkeeper to show you different colors or varieties?",
                "What polite English phrases can you use when negotiating a better price?",
                "How do you handle a return or exchange at a department store?"
            )
        ),
        PracticeTopic(
            id = "childhood_memories",
            title = "Cherished Childhood Memories",
            subtitle = "Street games, innocence, grandparents' stories, and memorable school days",
            category = "Social",
            iconName = "People",
            starterPrompt = "Whenever I think back to my childhood, one vivid memory that always brings a smile to my face is...",
            sampleQuestions = listOf(
                "What traditional street games or outdoor activities did you play as a child?",
                "Describe a special memory you shared with your grandparents or cousins.",
                "How has life changed compared to when you were growing up?"
            )
        ),
        PracticeTopic(
            id = "health_and_fitness",
            title = "Health, Fitness & Healthy Living",
            subtitle = "Morning workouts, nutritious meals, drinking water, and active energy",
            category = "Lifestyle",
            iconName = "FitnessCenter",
            starterPrompt = "Maintaining good physical health is vital for energy and positivity. My routine for staying active includes...",
            sampleQuestions = listOf(
                "What daily habits help you stay energetic and fit?",
                "Do you prefer gym workouts, yoga, sports, or brisk walking?",
                "How does physical fitness improve mental clarity and emotional wellness?"
            )
        ),
        PracticeTopic(
            id = "climate_and_nature",
            title = "Environment & Nature Protection",
            subtitle = "Planting trees, saving water, reducing plastic, and caring for our planet",
            category = "Modern",
            iconName = "Public",
            starterPrompt = "Nature gives us everything we need to thrive. We all have a responsibility to protect our environment by...",
            sampleQuestions = listOf(
                "What small daily steps can individuals take to reduce plastic waste?",
                "Why is planting trees and saving water crucial for future generations?",
                "How does spending time in nature recharge your mind and spirit?"
            )
        ),
        PracticeTopic(
            id = "managing_stress",
            title = "Managing Stress & Staying Calm",
            subtitle = "Techniques for handling exam or workplace pressure and staying positive",
            category = "Basics",
            iconName = "Psychology",
            starterPrompt = "When things become overwhelming, it is important to pause and breathe. The way I deal with pressure is...",
            sampleQuestions = listOf(
                "What causes you the most stress during busy weeks?",
                "What healthy habits help you relax and regain inner calm?",
                "How do you support a friend or colleague who is feeling stressed?"
            )
        ),
        PracticeTopic(
            id = "public_speaking_confidence",
            title = "Public Speaking & Overcoming Stage Fear",
            subtitle = "Building voice modulation, eye contact, body language, and stage courage",
            category = "Education",
            iconName = "RecordVoiceOver",
            starterPrompt = "Speaking before an audience can feel intimidating at first, but with steady practice and breath control...",
            sampleQuestions = listOf(
                "How do you prepare before giving a speech or presentation?",
                "What techniques help you overcome nervousness on stage?",
                "Why is clear body language and eye contact essential in communication?"
            )
        ),
        PracticeTopic(
            id = "books_and_inspiration",
            title = "Books & Stories That Inspired Me",
            subtitle = "Life lessons, favorite authors, biography insights, and inspiring quotes",
            category = "Interests",
            iconName = "School",
            starterPrompt = "One of the most impactful books I have ever read taught me that...",
            sampleQuestions = listOf(
                "Which book or story made a profound impression on your thinking?",
                "What was the core message or philosophy of the author?",
                "Why is daily reading one of the best habits for expanding vocabulary?"
            )
        ),
        PracticeTopic(
            id = "money_and_savings",
            title = "Money Management & Smart Savings",
            subtitle = "Budgeting, financial discipline, avoiding unnecessary spending, and future goals",
            category = "Lifestyle",
            iconName = "Payments",
            starterPrompt = "Managing personal finances wisely gives us peace of mind and security. My approach to saving money involves...",
            sampleQuestions = listOf(
                "Why is building an emergency fund important for young adults?",
                "How do you differentiate between essential needs and impulse desires?",
                "What is one smart financial habit you want to follow consistently?"
            )
        ),
        PracticeTopic(
            id = "teamwork_and_leadership",
            title = "Teamwork & Leadership",
            subtitle = "Collaborating with teammates, resolving disputes, and motivating others",
            category = "Professional",
            iconName = "Diversity3",
            starterPrompt = "Great achievements are rarely accomplished alone. When working in a team, true leadership means...",
            sampleQuestions = listOf(
                "What makes a team member reliable and easy to collaborate with?",
                "How would you address a disagreement between team members calmly?",
                "Describe a time you led or supported a group project successfully."
            )
        ),
        PracticeTopic(
            id = "cinema_and_movies",
            title = "Cinema, Movies & Storytelling",
            subtitle = "Memorable movies, brilliant acting, background scores, and emotional plots",
            category = "Entertainment",
            iconName = "Movie",
            starterPrompt = "Cinema is a powerful medium that connects emotions and stories. One of my favorite films is...",
            sampleQuestions = listOf(
                "What is your all-time favorite movie and what makes it unforgettable?",
                "Do you prefer thrilling action, heartfelt drama, or lighthearted comedy?",
                "How does cinema inspire people or influence society?"
            )
        ),
        PracticeTopic(
            id = "ai_and_future",
            title = "Artificial Intelligence & Future of Jobs",
            subtitle = "Smart algorithms, automated tools, human creativity, and future careers",
            category = "Modern",
            iconName = "AutoAwesome",
            starterPrompt = "Artificial Intelligence is transforming how we learn, code, design, and work across every industry...",
            sampleQuestions = listOf(
                "Which AI tools have you used and how did they help you learn?",
                "What human skills (like empathy and creative communication) can AI never replace?",
                "How can students prepare themselves for the future workplace?"
            )
        ),
        PracticeTopic(
            id = "handling_emergencies",
            title = "Handling Emergencies & Asking for Help",
            subtitle = "Explaining urgent situations, calling for assistance, and giving clear directions",
            category = "Everyday",
            iconName = "Emergency",
            starterPrompt = "Emergency situations require clear communication and a calm presence of mind. If an accident occurs, you must...",
            sampleQuestions = listOf(
                "How would you describe an emergency location and situation on the phone?",
                "What English phrases are essential when visiting an emergency clinic?",
                "How do you reassure someone who is panicked during an unexpected incident?"
            )
        ),
        PracticeTopic(
            id = "favorite_teacher",
            title = "My Favorite Teacher or Mentor",
            subtitle = "Expressing gratitude to an inspiring educator who believed in you",
            category = "Education",
            iconName = "School",
            starterPrompt = "A truly inspiring teacher does not just teach lessons, but shapes character. The teacher who inspired me most was...",
            sampleQuestions = listOf(
                "Who was your favorite teacher and what subject did they teach?",
                "What unique teaching style or quality made their classes memorable?",
                "What important life lesson did they pass on to you?"
            )
        ),
        PracticeTopic(
            id = "city_vs_village",
            title = "Life in a Big City vs Quiet Village",
            subtitle = "Opportunities, public transport, air quality, sense of community, and lifestyle",
            category = "Places",
            iconName = "LocationCity",
            starterPrompt = "Both city life and village life offer unique charms and distinct challenges. When comparing them...",
            sampleQuestions = listOf(
                "What are the best advantages of living in a modern metropolitan city?",
                "What makes village life peaceful, healthy, and heartwarming?",
                "If given a choice, where would you prefer to settle down in the long term?"
            )
        ),
        PracticeTopic(
            id = "overcoming_failure",
            title = "Overcoming Failure & Rising Strong",
            subtitle = "Patience, resilience, turning setbacks into stepping stones, and grit",
            category = "Social",
            iconName = "EmojiEvents",
            starterPrompt = "Failure is never the opposite of success; it is a vital part of the journey. A time I faced a setback was...",
            sampleQuestions = listOf(
                "Describe a situation where things did not go as planned.",
                "What important lessons did you discover about yourself during that time?",
                "How do you maintain a positive mindset when facing unexpected obstacles?"
            )
        ),
        PracticeTopic(
            id = "cultural_heritage",
            title = "Cultural Heritage & Traditions",
            subtitle = "Traditional attire, classical arts, folk music, festivals, and architecture",
            category = "Culture",
            iconName = "Celebration",
            starterPrompt = "Our cultural heritage connects us to centuries of history, wisdom, and celebration. One tradition I deeply cherish is...",
            sampleQuestions = listOf(
                "What cultural traditions or rituals are celebrated with pride in your family?",
                "Why is preserving traditional art forms, music, and cuisine important today?",
                "How do you share your rich culture with friends from different backgrounds?"
            )
        )
    )
}
