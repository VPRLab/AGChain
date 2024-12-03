import numpy as np
import matplotlib.pyplot as plt
import pandas as pd
import matplotlib as mpl
from matplotlib.markers import MarkerStyle

#
# global setting
# http://matplotlib.org/users/customizing.html

mpl.rcParams['lines.linewidth'] = 2
mpl.rcParams['lines.markersize'] = 10
mpl.rcParams['lines.markeredgewidth'] = 0.8
mpl.rcParams['markers.fillstyle'] = 'none'
mpl.rcParams['axes.labelsize'] = 18
mpl.rcParams['axes.labelpad'] = 6
mpl.rcParams['legend.fontsize'] = 20
mpl.rcParams['xtick.labelsize'] = 14
mpl.rcParams['ytick.labelsize'] = 14
#mpl.rcParams['figure.figsize'] = 5, 5

print (mpl.__version__)

#header=0 means the first line is header
# table1 = pandas.read_table('NewGasFee.csv', sep='\t', header=0)

xdata = []
ydata = []

# rawdata = table1.iloc(0)   # each row from the first row: 0, 1, 2
# print(table1)
# sortdata = np.sort(rawdata)
df = pd.read_csv('NewGasFee.csv')
lines_list = df["GasFee"]
sortdata = np.sort(lines_list)

xdata.append(sortdata)
ydata.append(np.arange(len(xdata[0]))/float(len(xdata[0])))

# http://stackoverflow.com/questions/22408237/named-colors-in-matplotlib
# '-^', '-v', '-o'
plt.plot(xdata[0], ydata[0], '-', lw=2, c='blue')
#plt.plot(xdata[1], ydata[1], '--', lw=4, c='red', mec='red')
#plt.plot(xdata[2], ydata[2], '-.', lw=4, c='lime', mec='lime')
#plt.plot(xdata[3], ydata[3], ':', lw=4, c='magenta', mec='magenta')

#plt.legend(['All', 'TCP', 'UDP'], loc='center right', numpoints=1)
plt.xlim((0.00008, 0.00013))
plt.ylim((0, 1))
#plt.axis([0, 15, 0, 1])
#plt.locator_params(axis='x', numticks=8)
#plt.locator_params(axis='y', numticks=4)
plt.ylabel("CDF")
plt.xlabel("Gas fee (Ether) per uploading transaction")
plt.grid(True)
plt.savefig('gasFee.pdf', transparent=True, bbox_inches='tight')
# plt.show()



def code_line_stat():
    """

    :param dbfile:
    :return:
    """
    # conn = sqlite3.connect(dbfile)
    # c = conn.cursor()

    # query_code_sql = "SELECT REPO, SHA, MAX(A_LINES, D_LINES) FROM CODE_HUNK " \
    #                  "WHERE IGNORE IS NULL"
    # query_code_results = c.execute(query_code_sql).fetchall()

    # lines_list = list()
    # for repo, sha, lines in query_code_results:
    #     lines_list.append(lines)

    df = pd.read_csv('NewGasFee.csv')
    lines_list = df["GasFee"]
    print(lines_list,max(lines_list))

    figzoom, axzoom = plt.subplots()
    axzoom.set(xlim=(0.00008, 0.000124608), ylim=(0, 1), autoscale_on=False)
    axzoom.hist(lines_list, bins=10000, histtype='step', cumulative=True, density=True)
    plt.xlabel('# Lines of Code Fragments')
    plt.ylabel('CDF')
    plt.grid(True)
    plt.show()

    # conn.close()
    return None
