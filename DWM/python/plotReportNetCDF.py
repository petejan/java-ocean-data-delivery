import Image
import matplotlib.pyplot as plt
import cStringIO
from reportlab.pdfgen import canvas
from reportlab.lib.units import inch, cm

from reportlab.lib.utils import ImageReader

fig = plt.figure(figsize=(4, 3))
plt.plot([1,2,3,4])
plt.ylabel('some numbers')

imgdata = cStringIO.StringIO()
fig.savefig(imgdata, format='png')
imgdata.seek(0)  # rewind the data

Image = ImageReader(imgdata)

c = canvas.Canvas('test.pdf')
c.drawImage(Image, cm, cm, inch, inch)
c.save()
