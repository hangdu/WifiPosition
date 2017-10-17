import statistics
from scipy.stats import norm

l = [2,3,4]
m = statistics.mean(l)
std = statistics.stdev(l)
x = 3
res = norm.pdf(x, m, std)
print(res)

