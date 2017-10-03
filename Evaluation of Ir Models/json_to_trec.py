# -*- coding: utf-8 -*-
"""
Thanks to the author Ruhan Sa, who is the TA of IR project 3 in Fall 2015
"""
print("In Python")
import json
import sys
import urllib.request
from collections import defaultdict

count = 1
langs = {'text_de','text_ru','text_en'}
core = sys.argv[2]
model = {'DFR', 'TFIDF', 'BM25'}
countRows = 0
outf = open(sys.argv[1] + '.txt', 'w+')
count = 1;
sortingList = []
QueryList = []
QueryDict = defaultdict(list)
writingDictionary = {}
with open('queries.txt', encoding="utf-8") as f:
	for line in f:	
		query = line.strip('s\n').replace(':', '')[4:]
		query = urllib.parse.quote(query)
		countRows = 1
		for lang in langs:
			inurl = ('http://ec2-35-163-27-180.us-west-2.compute.amazonaws.com:8983/solr/' + core + 
					'/select?df=' + lang + '&fl=score,id&indent=on&q=' + query + '&rows=20&wt=json')
			qid = count
			IRModel = core
			data = urllib.request.urlopen(inurl).read()
			docs = json.loads(data.decode('utf-8'))['response']['docs']
			rank = 1
			for doc in docs:
				countRows += 1
				if qid == 10 or qid == 20:
					score = float((str(doc['score']) + str(countRows) + '5'))
					sortingList.append(score)
					writingDictionary.update({str('Q' + str(count) + str(score)): str(doc['id'])})
				else:
					score = float((str(doc['score']) + str(countRows)))
					sortingList.append(score)
					writingDictionary.update({str('Q' + str(count) + str(score)): str(doc['id'])})
		sortingList.sort(reverse=True)
		QueryDict.setdefault('Q' + str(count), sortingList)
		QueryList.append('Q' + str(count))
		sortingList = []
		count += 1
countRows = 1
count = 1
for query in QueryList:
	for key in QueryDict.get(query):
		print('Q' + str(count) + str(key))
		if countRows > 20:
			break
		else:
			if count > 9:	
				outf.write('0' + str(count) + ' ' + 'Q' + str(count) + ' ' + writingDictionary.get('Q' + str(count) + str(key)) + ' ' + str(countRows) + ' ' + str(key)[:-1] + ' ' + core + '\n')
			else:
				outf.write('00' + str(count) + ' ' + 'Q' + str(count) + ' ' + writingDictionary.get('Q' + str(count) + str(key)) + ' ' + str(countRows) + ' ' + str(key)[:-1] + ' ' + core + '\n')
			countRows += 1
	count += 1
	countRows = 1
outf.close()