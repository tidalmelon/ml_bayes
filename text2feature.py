#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
1, 文本文件转特征集
2, 80作为训练数据，20%作为测试数据
"""

import os
import re
import random

INPUTDIR = 'input'

TRAINING_PERCENT = 0.8
WORD_LIST = []
WORD_ID_DIC = {}

FILE_TRAIN = open('train.dat', 'w')
FILE_TEST = open('test.dat', 'w')

pat = re.compile('\d+(\w+)\.\w+\.\w+\.\w+')
files = os.listdir(INPUTDIR)
for fname in files:
    classname = pat.search(fname).group(1)

    out = FILE_TEST
    rd = random.random()
    if rd < TRAINING_PERCENT:
        out = FILE_TRAIN
    out.write(str(classname) + ' ')

    fname = os.path.join(INPUTDIR, fname)
    with open(fname) as f:
        while True:
            line = f.readline()
            if not line:
                break
            line = line.strip()
            words = line.split()
            for word in words:
                if not word:
                    continue
                if word not in WORD_ID_DIC:
                    WORD_LIST.append(word)
                    WORD_ID_DIC[word] = len(WORD_LIST)

                out.write(str(WORD_ID_DIC[word]) + ' ')
    out.write(os.linesep)

print '%d unique words found' % len(WORD_LIST)

FILE_TEST.close()
FILE_TEST.close()
