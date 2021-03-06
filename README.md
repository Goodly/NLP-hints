
# Text Thresher Hints

NLP-Hints uses nlp techniques such as named entity recognition to provide hints to questions about the selected text in the Text Thresher Interface.

## Setup (for Heideltime)

In configs.props, set the path to TreeTagger home directory which is used in heideltime. This is currently what it looks like for me.  

```
treeTaggerHome = /Users/manishasharma/Desktop/temp-tagger/heideltime-standalone/
```


#### Running Annotator.java.

```
mvn clean compile
mvn -q exec:java -Dexec.mainClass="annotator.Annotator" > out.json -Dexec.args="in.json"
```

127.001:5000/api/hints 

#### Example Input

```json
[{
	"Topic Text": "DENVER - Occupy Denver planned to hold a dance and march to mark the one year
	anniversary of the Occupy movement's start in New York. On Monday, Occupy supporters planned what
	they called a foreclosure dance outside the Wells Fargo bank on the 16th Street Mall in Denver
	Supporters were invited to join in from noon to the close of business. Protesters then planned to
	march to Civic Center Park for what they called an after-party event from 6 to 11 p.m. Despite
	clashes with police last year, the Colorado State Patrol and Denver Police said they did not plan to
	add an extra staff at the park Monday night. While the Occupy Denver movement has not been as visible
	lately, members say they're still very busy. It's becoming more of an organized entity, said Corrine
	Fowler. They're meeting weekly, they have teach-ins on many different social justice issues, they
	have a number of different committees, they're fundraising. Just because you don't see them pounding
	the pavement demanding change, they're in there working on it, Fowler explained.",
	"Questions": [{
		"ID": 1,
		"Question": "Where did the protest start?"
	}, {
		"ID": 2,
		"Question": "Where did the protestors plan to march to?"
	}]
}, {
	"Topic Text": "The Occupy Denver movement began about 10 days after the New York protests began. By
	October 2011, hundreds were attending marches and protests in Denver's Civic Center Park, 16th Street
	Mall and in between. A few days later, a make-shift camp appeared across from the state Capitol
	[across from the state Capitol]. By October 12, the camp in Veteran's Park had grown to more than 80
	tents. After the governor ordered the park closed, protesters and officers clashed on October 14 when
	officers moved in to remove the tents. Two dozen people were arrested. The protests continued
	throughout the winter with a handful of members of Occupy Denver living on the sidewalk next to Civic
	Center Park on Broadway between Colfax and 14th avenues. Then in March, the area had to be cleared
	out after one of the inhabitants told authorities he had scabies. In May, the city of Denver banned
	unauthorized camping in the city.",
	"Questions": [{
		"ID": 1,
		"Question": "How many people were arrested?"
	}, {
		"ID": 2,
		"Question": "When was camping banned?"
	}, {
      "ID": 3,
      "Question": "How many tents were there?"
   }]
}]
```

#### Example Output


``` json
[
  {
    "topicID": 1,
    "Hints": [
      {
        "Highlights": [
          "DENVER",
          "Denver",
          "New York",
          "Wells Fargo",
          "16th Street Mall",
          "Denver",
          "Civic Center Park",
          "Denver"
        ],
        "Indices": [
          [
            0,
            6
          ],
          [
            16,
            22
          ],
          [
            124,
            132
          ],
          [
            220,
            231
          ],
          [
            244,
            260
          ],
          [
            264,
            270
          ],
          [
            379,
            396
          ],
          [
            628,
            634
          ]
        ],
        "qID": 1
      },
      {
        "Highlights": [
          "DENVER",
          "Denver",
          "New York",
          "Wells Fargo",
          "16th Street Mall",
          "Denver",
          "Civic Center Park",
          "Denver"
        ],
        "Indices": [
          [
            0,
            6
          ],
          [
            16,
            22
          ],
          [
            124,
            132
          ],
          [
            220,
            231
          ],
          [
            244,
            260
          ],
          [
            264,
            270
          ],
          [
            379,
            396
          ],
          [
            628,
            634
          ]
        ],
        "qID": 2
      }
    ]
  },
  {
    "topicID": 2,
    "Hints": [
      {
        "Highlights": [
          "dozen people"
        ],
        "Indices": [
          [
            529,
            541
          ]
        ],
        "qID": 1
      },
      {
        "Highlights": [
          "about 10 days",
          "October 2011",
          "A few days later",
          "October 12",
          "October 14",
          "winter",
          "March",
          "May"
        ],
        "Indices": [
          [
            33,
            46
          ],
          [
            3,
            15
          ],
          [
            0,
            16
          ],
          [
            3,
            13
          ],
          [
            79,
            89
          ],
          [
            38,
            44
          ],
          [
            8,
            13
          ],
          [
            3,
            6
          ]
        ],
        "qID": 2
      },
      {
        "Highlights": [
          "80 tents",
          "the tents"
        ],
        "Indices": [
          [
            381,
            389
          ],
          [
            514,
            523
          ]
        ],
        "qID": 3
      }
    ]
  }
]

```
