#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import re

INPUTDIR = 'input'

# 计算先验概率
CLASS_FREQ_DIC = {}

WORDLIST = []
WORD_ID_DIC = {}

pat = re.compile('\d+(\w+)\.\w+\.\w+\.\w+')
files = os.listdir(INPUTDIR)
for fname in files:
    classname = pat.search(fname).group(1)
    if classname not in CLASS_FREQ_DIC:
        CLASS_FREQ_DIC[classname] = 1
    else:
        CLASS_FREQ_DIC[classname] += 1

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
                    WORDLIST.append(word)
                    WORD_ID_DIC[word] = len(WORDLIST)

