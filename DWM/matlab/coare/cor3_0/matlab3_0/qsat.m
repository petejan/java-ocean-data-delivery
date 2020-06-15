function y=qsat(y)
x=y(:,1);%temp
p=y(:,2);%pressure
es=6.112.*exp(17.502.*x./(x+241.0)).*(1.0007+3.46e-6*p);
y=es*622./(p-.378*es);