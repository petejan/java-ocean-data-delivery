c$debug
c.......................................................................
c COARE Bulk Flux Algorithm version 3.0a. See Fairall et al.,2003: J.Climate,16,571-591
c
c Written 05/09/2003 by Frank Bradley frank.bradley@csiro.au
c
c Some details of changes from versions 2.5 and 2.6.  For full description see Cor30_readme.txt
c
c Wave options for Zo - models by Taylor and Yelland (JPO 2001,31,572) or Oost et al.(BLM 2002,103,409)
c Wind speed dependant Charnock parameter above 10 m/s
c Grachev and Fairall first guess z/L routine (JAM 36,406-414,1997) to cut iterations from 20 to 3
c rr-zoq,charnock relations as described in COARE 3.0 J. Climate paper (see above)
c Use separate phiu,phit functions with Grachev et al. (2000) constants
c Stable functions according to Beljaars and Holtslag (JAM 30, 327-341,1991)
c Return Webb Wbar because Webb flux correction already included in latent heat flux
c
c Set up to read COARE test data set, test3_0.txt with 4-digit year tab delimited
c see Bradley, Moncrieff and Weller, Proceedings of the COARE Flux Group workshop, May 14-16 1997,
c NCAR/MMM, PO Box 3000, Boulder, CO 80307-3000, USA 
c
c THIS version "h" uses Mike Gregg's ASP Ts data at 6m depth, prepared by Hemantha Wijesekera
c Another version "f" uses Chris Fairall's seasnake for Ts nominal depth 0.05m
c Outputs tst3_0af.out or tst3_0ah.out written from subroutine bulk_flux
c Wave option outputs tst30afo.out or tst30aft.out (Jwave = 1 or 2)
c......................................................
c   input data - introduced in calling program "fluxes" and passed to subroutines in COMMON
c     xtime (COARE convention yyyymnddhhmmss.ss GMT)
c     u (wind speed relative to the sea surface) m/s  i.e. add CURRENT VECTOR if available
c     ts (sea temperature)  deg. C
c     t (air temperature) deg. C
c     q (specific humidity) g/kg or RH (as decimal) - code detects which then works internally in kg/kg
c     rs (shortwave radiation) W/m2
c     rl (downwelling longwave) W/m2
c     rain (average rainrate in timestep) mm/hour
c     xlat (latitude) degrees  [latitude north +ve, latitude south -ve]
c     xlon (longitude) degrees [longitude east +ve, longitude west -ve]
c     hwt alternative sea temperature (6m depth)
c     p (pressure) mb; use 1008mb if data unavailable
c     zi (boundary-layer depth) m; use 600m if data unavailable
c     zu (wind measurement height) m   
c     zt (T measurement height) m  
c     zq (q measurement height) m  
c     zus (wind standard height) m - usually 10m
c     zts (T and q standard height) m - "   
c     ts_depth (depth of ts measurement) - positive m - for warm layer correction 
c     jcool (=1 for cool skin calculation;  =0 if SST measured by IR radiometer)
c     jwarm (=1 for warm layer calculation; =0 if SST measured by IR radiometer)
c     jwave (wave state options 0=Charnock,1=Oost et al,2=Taylor and Yelland)
c....................................................................
c   calculated inputs: qs (humidity at ocean interface) and tsw (ts with warm layer added)
c....................................................................
c   outputs available - write statements in subroutine bulk_flux
c     hf W/m**2     (Sensible heat flux) - turbulent part only
c     ef W/m**2     (Latent heat flux) - turbulent part only
c     rf W/m**2     (Heat flux due to rainfall )
c     tau N/m**2   (wind stress) - turbulent part only
c     taur - momentum flux due to rain N/m**2
c     usr m/s     (M-O velocity scaling parameter u* = friction velocity)
c     tsr C       (M-O temperature scaling parameter t*)
c     qsr kg/kg   (M-O humidity scaling parameter q*)
c     Cdn - neutral drag coefficient
c     Chn - neutral transfer coefficient for heat
c     Cen - neutral transfer coefficient for moisture
c     RR - Roughness Reynolds number
c     RT - Roughness Reynolds number for temperature
c     RQ - Roughness Reynolds number for moisture
c     zL - stability parameter height/L where L is the Obukhov length
c     zo - velocity roughness length
c     zot - temperature roughness length
c     zoq - humidity roughness length
c     dt_wrm - total warm layer temperature difference C
c     tk_pwp - thickness of warm layer
c     dsea - (=dt_wrm*ts_depth/tk_pwp) warming above sea temperature measurement
c     dter - cool skin temperature difference C 
c     tkt - cool skin thickness mm (printed out *1000)
c     sst - skin temperature C (sst = ts - dter + dsea)
c     Wg - gustiness factor m/s
c     Wbar - Webb mean vertical velocity m/s
c     u_zs - wind velocity at standard height
c     t_zs - air temperature ditto
c     q_zs,qa_zs,e_zs,rh_zs - humidity ditto in a variety of units
c
      program fluxes
c
      real*8 xtime,u,ts,t,q,rs,rl,rain,xlat,xlon,hwt,qs,tsw
      real*8 zu,zt,zq,ts_depth,zs,p,zi,hwave,twave
      real*8 fxp,tk_pwp,qcol_ac,tau_ac
c
      COMMON/indata/xtime,u,ts,t,q,rs,rl,rain,xlat,xlon,qs,tsw !qs added
      COMMON/warm/qcol_ac,tau_ac,jamset,jday1,fxp,tk_pwp
      COMMON/heights/zu,zt,zq,ts_depth,zs,p,zi
      COMMON/options/Jwarm,Jcool,Jwave
      COMMON/wave/hwave,twave       
c
c open input/output files
      open(unit=3,file='test3_0.txt') ! 4-day coare test data
      open(unit=4,file='tst3_0ah.out')  ! output no wave data used
c
c  set up fixed instrument levels
      zu=15.               !height of wind measurement Moana Wave COARE
      zt=15.               !height of air temp MW COARE
      zq=15.               !height of humidity MW COARE
c      ts_depth=0.05        !Chris Fairall's floating sensor (SeaSnake)
c      ts_depth=0.45        !from IMET buoy
      ts_depth=6.0         !Hemantha's 6m data *** ALSO un-comment ts=hwt statement below ***
c
c  default values for standard height (normally 10m) pressure and mixed layer height
      zs=10.
      p=1008.
      zi=600.
c
c if SST sensed by IR radiometer, set jwarm and jcool to zero
      jwarm=1
      jcool=1
      jwave=0   !0=Charnock,1=Oost et al,2=Taylor and Yelland.
c
c initialize warm layer variables and switches
      qcol_ac=0.
      tau_ac=0.
      fxp=0.5
      tk_pwp=19.0
      jamset=0
      jday1=1
c
c loop through data
c
      index=0
      do     !*****start data input loop*****
      index=index+1              !count data records (hours for test data) 
c      
c read Chris Fairall's Moana Wave data from test file, 116 lines(hours)
c ts is Chris' floating temperature sensor at 0.05m depth
c hwt=Hemantha's Gregg data at 6m for Ts to demonstrate warm layer calculation
c
c free format for tab delimited test2_6b.txt
c
700   read(3,*,end=910)xtime,u,ts,t,q,rs,rl,rain,xlat,xlon,hwt
c
cFor fixed format version test2_6b.dat.
c
c700   READ(3,705,end=910)xtime,u,ts,t,q,rs,rl,rain,xlat,xlon,hwt                          
c705   FORMAT(f14.0,10F8.0)
c
      ts=hwt     !un-comment to use 6m depth sea temperature; also change ts_depth above
c
c default values for wave height and period for equilibrium sea
c if known, add to data and to read statement above,then comment next two lines
c
      hwave=0.018*u*u*(1.+.015*u)
      twave=0.729*u
c
c   ******* call bulk flux routine *******
c                    
      call bulk_flux(index)
c
      write(*,*) index
c     
      enddo                !return to beginning of loop
910   continue   
c
c end of loop
c
      stop
      end
c
c*****************************************************************
      subroutine bulk_flux(index)
c
      real*8 u_zs,t_zs,q_zs,e_zs,esat_zs,qa_zs
      real*8 hf,ef,tau,rf,taur
      real*8 CD,CE,CH,usr,tsr,qsr
      real*8 zo,zot,zoq,RR,RT,RQ,RI,zL
      real*8 sst,ea,es,rns,rnl
c various constants
      real*8 al,be,cpa,cpw,cpv,grav,xlv,rhoa,rhow,rgas,toK,von
c      real*8 visa,visw,von
c rain heat variables
      real*8 alfac,wetc,dwat,dtmp
c warm layer variables
      real*8 intime,sol_time,time_old,ctd1,ctd2,rich
      real*8 qcol_ac,tau_ac,tau_old,hf_old,ef_old,rf_old
      real*8 fxp,tk_pwp,dsea,qjoule,qr_out,dtime
      real*8 dt_wrm,dter,dqer,tkt
c
      real*8 xtime,u,ts,t,q,p,zi,rain,xlat,xlon,qa,qs,tsw
      real*8 zu,zt,zq,zs
      real*8 rl,rs,ts_depth,psiu,psit
      real*8 Cdn,Cen,Chn
      real*8 Du,Wg
      real*8 Wbar
      integer hh,yy,dd,ss
      character*19 chtime   !only works with 19
c      
      COMMON/indata/xtime,u,ts,t,q,rs,rl,rain,xlat,xlon,qs,tsw        !qs added
      COMMON/warm/qcol_ac,tau_ac,jamset,jday1,fxp,tk_pwp
      COMMON/const/al,be,cpa,cpw,grav,xlv,rhoa,rhow,rgas,toK,von,wetc
      COMMON/heights/zu,zt,zq,ts_depth,zs,p,zi
      COMMON/options/Jwarm,Jcool,Jwave
      COMMON/rad/rns,rnl
      COMMON/ASLout/usr,tsr,qsr,zo,zot,zoq,zL,RR,RT,RQ,RI,
     &             dter,dqer,tkt,Du,Wg
c
      if(index.eq.1) then   !first line of data
       tsw=ts               !henceforth redefined after warm/cool calculations
       dter=0.3*jcool       !or in "if" block below when jwarm=0.
       sst=ts-dter          !for initial Rnl
      endif                        
c
c Constants and coefficients (Stull 1988 p640). 
      Rgas=287.1                    !J/kg/K     gas const. dry air
      toK=273.16                    ! Celsius to Kelvin
      Cpa=1004.67                   !J/kg/K specific heat of dry air (Businger 1982)
      Cpv=Cpa*(1.+0.00084*q)         !Moist air - currently not used (Businger 1982)
      rhoa=P*100./(Rgas*(t+toK)*(1.+.00061*q)) !kg/m3  Moist air density NB q still g/kg
      call gravity(xlat,grav)       ! gravity equatorial value 9.72
      al=2.1e-5*(ts+3.2)**0.79      !water thermal expansion coefft.
      be=0.026                      !salinity expansion coefft.
      cpw=4000.                     !J/kg/K specific heat water
      rhow=1022.                    !kg/m3  density water
      von=0.4                       !von Karman's "constant"
c
c   compute net radiation, updated in flux loop of ASL
c   oceanic emissivity 0.97 broadband; albedo 0.055 daily average
      Rnl= 0.97*(5.67e-8*(sst+toK)**4-rl) !Net longwave (up = +). Typically 3C warming=15W/m2
      Rns=0.945*rs                        !Net shortwave (into water)
c
c     START Warm Layer - check switch
c            
      if(Jwarm.eq.0) then  !jump over warm layer calculation
       tsw=ts
       go to 15            !convert humidities and calculate fluxes in ASL
      endif
c
c  convert time to decimal hours (yyyymnddhhmmss. -> hh.hhhh)
c
       write(chtime(1:19),'(f19.4)') xtime     
       read(chtime,12) yy,mn,dd,hh,mm,ss       !eg 1992,11,25,13,21,00
12     format(i4,5i2)  
       intime=(float(hh)+float(mm)/60.+ss/3600.) !eg 13.35
c
c   and then to local solar time in seconds
c
       sol_time=mod(xlon/15+intime+24,24.)*3600   !eg 85517.
       sol=sol_time/3600.
c
       if(index.eq.1) go to 16         !first line of data. Set time_old and compute fluxes in ASL
       if(sol_time.lt.time_old) then   !reset all variables at local midnight
          jday1=0                      !reset after last 6am test of first day
          jamset=0                     !indicates heat integration not yet started
          tau_ac=0.0
          qcol_ac=0.0
          dt_wrm=0.0  
c initial guess at warm layer parameters expected in early morning
c fxp=0.5 implies a shallow heating layer to start the integration;
c tk_pwp=19.0 implies warm layer thickness is a maximum from the day
c before and is not meant to match this timestep's fxp.
          fxp=0.5
          tk_pwp=19.0
          tsw=ts
          goto 16                      
        else if(sol_time.gt.21600..and.jday1.eq.1) then   !6 am too late to start on first day
          dt_wrm=0.
          tsw=ts
          goto 16
        else                       !compute warm layer. Rnl and "_old"s from previous timestep
          rich=.65                                    !critical Rich. No.
          ctd1=sqrt(2.*rich*cpw/(al*grav*rhow))        !u*^2 integrated so
          ctd2=sqrt(2.*al*grav/(rich*rhow))/(cpw**1.5) !has /rhow in both
          dtime=sol_time-time_old                      !time step for integrals
          qr_out=rnl+hf_old+ef_old+rf_old              !total cooling at surface
          q_pwp=fxp*rns-qr_out                         !total heat absorption in warm layer
           if(q_pwp.lt.50..and.jamset.eq.0) then       !integration threshold
           tsw=ts
           go to 16
           endif
          jamset=1                                    !indicates integration has started
          tau_ac=tau_ac+max(.002,tau_old)*dtime       !momentum integral
          if(qcol_ac+q_pwp*dtime.gt.0) then           
            do 10 iter1=1,5                           !iterate warm layer thickness
              fxp=1.-(0.28*0.014*(1.-dexp(-tk_pwp/0.014))   
     &            +0.27*0.357*(1.-dexp(-tk_pwp/0.357))         
     &            +.45*12.82*(1.-dexp(-tk_pwp/12.82)))/tk_pwp !Soloviev solar absorb. prof
              qjoule=(fxp*rns-qr_out)*dtime
              if((qcol_ac+qjoule.gt.0.0))
     &         tk_pwp=min(19.,ctd1*tau_ac/sqrt(qcol_ac+qjoule))  !warm layer thickness
   10       continue
          else                                        !warm layer wiped out
            fxp=.75
            tk_pwp=19.
            qjoule=(fxp*rns-qr_out)*dtime             
          endif                                       
          qcol_ac=qcol_ac+qjoule                      !heat integral
          if(qcol_ac.gt.0) then                       !sign check on qcol_ac
            dt_wrm=ctd2*(qcol_ac)**1.5/tau_ac         !pwp model warming
          else
            dt_wrm=0.
          endif         
        endif
        if(tk_pwp.lt.ts_depth) then           !sensor deeper than pwp layer
          dsea=dt_wrm                         !all warming must be added to ts
        else                                  !warming deeper than sensor
          dsea=dt_wrm*ts_depth/tk_pwp         !assume linear temperature profile
        endif
        tsw=ts+dsea                           !add warming above sensor to ts
c
   16   time_old=sol_time                     !all in local solar time
c
c     END of warm layer - convert humidities
c
   15 call humidity(t,p,ea)         !Teten's returns sat. air qa in mb
      if(q.lt.2.) then              !checks whether humidity in g/Kg or RH      
         R=q
         ea=ea*R                    !convert from RH using vapour pressure      
         q=.62197*(ea/(p-0.378*ea)) !Spec. humidity kg/kg
      else
         q=q/1000.                  !g/kg to kg/kg
      endif
      qa=.62197*(ea/(p-0.378*ea))   !convert from mb to spec. humidity  kg/kg
      call humidity(tsw,p,es)       !sea qs returned in mb
      es=es*0.98                    !reduced for salinity Kraus 1972 p. 46
      qs=.62197*(es/(p-0.378*es))   !convert from mb to spec. humidity  kg/kg
c
      call ASL(index)
c
c compute surface fluxes and other parameters
       sst=tsw-dter*jcool             !final skin temperature this timestep
       tau=rhoa*usr*usr*u/Du          !stress N/m2
       hf=-cpa*rhoa*usr*tsr           !sensible W/m2
       ef=-xlv*rhoa*usr*qsr           !latent W/m2
c compute heat flux due to rainfall
       dwat=2.11e-5*((t+toK)/toK)**1.94                    !water vapour diffusivity
       dtmp=(1.+3.309e-3*t-1.44e-6*t*t)*0.02411/(rhoa*cpa) !heat diffusivity
       dqs_dt=qa*xlv/(rgas*(t+toK)**2)                     !Clausius-Clapeyron
       alfac= 1./(1.+(wetc*xlv*dwat)/(cpa*dtmp))     !wet bulb factor
       rf= rain*alfac*cpw*((sst-t)+(qs-q-dqer)*xlv/cpa)/3600.
c compute momentum flux due to rainfall
       taur=0.85*rain/3600*u  
c Webb correction to latent heat flux already in ef via zoq/rr function so return Wbar
       Wbar=-1.61*usr*qsr/(1.+1.61*q)-usr*tsr/(t+toK)
c save fluxes for next timestep warm layer integrals
       tau_old=tau 
       ef_old=ef
       hf_old=hf
       rf_old=rf
c compute transfer coefficients
       CD=(USR/Du)**2
       CH=USR*TSR/(Du*(T-sst+.0098*zt)) 
       CE=USR*QSR/(Du*(Q-QS+dqer))                                      
c compute neutral transfer coefficients and met variables at standard height
       Cdn=(0.4/dlog(zs/zo))**2
       Chn=0.4*0.4/(dlog(zs/zo)*dlog(zs/zot))
       Cen=0.4*0.4/(dlog(zs/zo)*dlog(zs/zoq))
c adjust met. variables to standard height
       u_zs=usr/von*(dlog(zs/zo)-psiu(zL))
       t_zs=sst+tsr/von*(dlog(zs/zot)-psit(zL))
       q_zs=(qs-dqer)+qsr/von*(dlog(zs/zoq)-psit(zL)) !kg/kg
       qa_zs=1000.*q_zs                               !g/kg
       e_zs=q_zs*p/(0.62197+0.378*q_zs)               !mb
       call humidity(t_zs,p,esat_zs)                  !mb
       rh_zs=e_zs/esat_zs                             !RH as decimal
c
c output fluxes
c     
      write(4,200)index,xtime,hf,ef,sst,tau,Wbar,rf,
     & dter,dt_wrm,tk_pwp,tkt*1000.,Wg
200   format(i6,',',f18.0,3(',',f8.2),2(',',f9.5),6(',',f8.2))
c
      return      !return to main program for another line of data
      end
c
c ------------------------------------------------------------------
      subroutine ASL(index)
c
c TO EVALUATE SURFACE FLUXES, SURFACE ROUGHNESS AND STABILITY OF
c THE ATMOSPHERIC SURFACE LAYER FROM BULK PARAMETERS BASED ON
c LIU ET AL. (79) JAS 36 1722-1735 
c    
      real*8 xtime,u,t,q,ts,qs,rs,rl,rain,xlat,xlon,tsw
      real*8 rns,rnl,ZU,ZT,ZQ,zi,p,Du,Wg,ts_depth,zs
      real*8 usr,tsr,qsr,zo,zot,zoq,zL,RR,RT,RQ,RI,zetu,L10,L
c constants
      real*8 al,beta,cpa,cpw,grav,xlv,rhoa,rhow,rgas,toK,visa
      real*8 visw,von,charn,psiu,psit
c cool skin quantities
      real*8 wetc,bigc,be,dter,dqer,tkt,Bf,tcw
      real*8 hsb,hlb,alq,qcol,qout,dels,xlamx,dq,dt,ta
c Grachev and Fairall variables
      real*8 u10,zo10,cd10,ch10,ct10,zot10,cd,ct,cc,ribcu,ribu
      real*8 hwave,twave,cwave,lwave,twopi
c
      COMMON/indata/xtime,u,ts,t,q,rs,rl,rain,xlat,xlon,qs,tsw        !qs added
      COMMON/const/al,be,cpa,cpw,grav,xlv,rhoa,rhow,rgas,toK,von,wetc
      COMMON/heights/zu,zt,zq,ts_depth,zs,p,zi
      COMMON/options/Jwarm,Jcool,Jwave
      COMMON/wave/hwave,twave       
      COMMON/rad/rns,rnl
      COMMON/ASLout/usr,tsr,qsr,zo,zot,zoq,zL,RR,RT,RQ,RI,
     &             dter,dqer,tkt,Du,Wg     
c
c Factors
      Beta=1.2     !Given as 1.25 in Fairall et al.(1996)
      twopi=3.14159*2.
c 
c Additional constants needed for cool skin
      visw=1.e-6                   !m2/s kinematic viscosity water
      tcw=0.6                      !W/m/K   Thermal conductivity water
      bigc=16.*grav*cpw*(rhow*visw)**3/(tcw*tcw*rhoa*rhoa)
      xlv=(2.501-0.00237*tsw)*1e+6 !J/kg latent heat of vaporization at ts (3C warming=0.3%)
      wetc=0.622*xlv*qs/(rgas*(tsw+toK)**2) !Clausius-Clapeyron
      visa=1.326e-5*(1.+6.542e-3*t+8.301e-6*t*t-4.84e-9*t*t*t)   !m2/s
          !Kinematic viscosity of dry air - Andreas (1989) CRREL Rep. 89-11
      ta=t+toK     !air temperature K
c 
c Wave parameters
      cwave=grav*twave/twopi
      lwave=cwave*twave
c
c Initial guesses
      dter=0.3*jcool              !cool skin Dt
      dqer=wetc*dter              !cool skin Dq
      zo=0.0001
      Wg=0.5                      !Gustiness factor initial guess
      tkt= 0.001*jcool                  !Cool skin thickness first guess
c
c Air-sea differences - includes warm layer in Dt and Dq
      Du=(u**2.+Wg**2.)**.5       !include gustiness in wind spd. difference
      Dt=tsw-t-0.0098*zt          !potential temperature difference.
      Dq=qs-q                     
c
c **************** neutral coefficients ******************
c
      u10=Du*dlog(10/zo)/dlog(zu/zo)
      usr=0.035*u10
      zo10=0.011*usr*usr/grav+0.11*visa/usr
      Cd10=(von/dlog(10/zo10))**2
      Ch10=0.00115
      Ct10=Ch10/sqrt(Cd10)
      zot10=10./dexp(von/Ct10)
      Cd=(von/dlog(zu/zo10))**2
c      
c ************* Grachev and Fairall (JAM, 1997) **********
c
      Ct=von/dlog(zt/zot10)         ! Temperature transfer coefficient
      CC=von*Ct/Cd                  ! z/L vs Rib linear coefficient
      Ribcu=-zu/(zi*0.004*Beta**3)  ! Saturation or plateau Rib 
      Ribu=-grav*zu*((Dt-dter)+0.61*ta*Dq)/(ta*Du**2)
      if (Ribu.lt.0.) then
          zetu=CC*Ribu/(1.+Ribu/Ribcu)   ! Unstable G and F
      else
          zetu=CC*Ribu*(1.+27./9.*Ribu/CC) ! Stable
      endif
      L10=zu/zetu                       ! MO length
      if (zetu.gt.50) then
        nits=1
      else
        nits=3   ! number of iterations
      endif
c
c First guess M-O stability dependent scaling params.(u*,t*,q*) to estimate zo and z/L
c
      usr= Du*von/(dlog(zu/zo10)-psiu(zu/L10))
      tsr=-(Dt-dter)*von/(dlog(zt/zot10)-psit(zt/L10))
      qsr=-(Dq-dqer)*von/(dlog(zq/zot10)-psit(zq/L10))
c      
      charn=0.011     !then modify Charnock for high wind speeds Chris' data
      if(Du.gt.10) charn=0.011+(0.018-0.011)*(Du-10)/(18-10)
      if(Du.gt.18) charn=0.018
c      
c **** Iterate across u*(t*,q*),zo(zot,zoq) and z/L including cool skin ****
c
      do 10 iter=1,nits
       if(Jwave.eq.0) then
        zo=charn*usr*usr/grav + 0.11*visa/usr    !after Smith 1988
       else if(Jwave.eq.1) then
        zo=(50./twopi)*lwave*(usr/cwave)**4.5+0.11*visa/usr !Oost et al.
       else if(Jwave.eq.2) then
        zo=1200.*hwave*(hwave/lwave)**4.5+0.11*visa/usr !Taylor and Yelland 
       endif 
      rr=zo*usr/visa
c
c *** zoq and zot fitted to results from several ETL cruises ************
c
      zoq=min(1.15e-4,5.5e-5/rr**0.6)
      zot=zoq
c
	zL=von*grav*zu*(tsr*(1.+0.61*q)+0.61*ta*qsr)
     &   /((t+toK)*usr*usr*(1.+0.61*q))
      L=zu/zL
      psu=psiu(zu/L)
      pst=psit(zt/L)
      dqer=wetc*dter*jcool
      usr=Du*von/(dlog(zu/zo)-psiu(zu/L))
      tsr=-(Dt-dter)*von/(dlog(zt/zot)-psit(zt/L))
      qsr=-(Dq-dqer)*von/(dlog(zq/zoq)-psit(zq/L))
      Bf=-grav/ta*usr*(tsr+0.61*ta*qsr)
       if (Bf.gt.0) then
          Wg=Beta*(Bf*zi)**.333
       else
          Wg=0.2
       endif
         Du=sqrt(u**2.+Wg**2.)        !include gustiness in wind spd.
c      
      rnl= 0.97*(5.67e-8*(tsw-dter+toK)**4-rl)  !Recompute net longwave; cool skin=-2W/m2
c
c   Cool skin
c
         if(Jcool.eq.0) go to 10
           hsb=-rhoa*cpa*usr*tsr
           hlb=-rhoa*xlv*usr*qsr
           qout=rnl+hsb+hlb
           dels=rns*(.065+11.*tkt-6.6e-5/tkt*(1.-dexp(-tkt/8.0e-4))) !Eq.16 Ohlmann 
           qcol=qout-dels
         alq=Al*qcol+be*hlb*cpw/xlv                      !Eq. 7 Buoy flux water
         if(alq.gt.0.) then                              !originally (qcol.gt.0)
           xlamx=6./(1.+(bigc*alq/usr**4)**.75)**.333      !Eq 13 Saunders coeff.
           tkt=xlamx*visw/(sqrt(rhoa/rhow)*usr)          !Eq.11 Sublayer thickness
         else
           xlamx=6.                                      !prevent excessive warm skins
           tkt=min(.01,xlamx*visw/(sqrt(rhoa/rhow)*usr)) !Limit tkt
         endif
       dter=qcol*tkt/tcw                                 ! Eq.12 Cool skin
       dqer=wetc*dter
   10 continue                                           ! end iterations
c
      idum=index          ! avoids warning on compilation
      return              !to main subroutine, bulk_flux
      end
c
c------------------------------------------------------------------
      subroutine humidity(T,P,esat)                                 
c
c Tetens' formula for saturation vp Buck(1981) JAM 20, 1527-1532 
c     
      real*8 T,P,esat
c     
      esat = (1.0007+3.46e-6*P)*6.1121*dexp(17.502*T/(240.97+T)) !mb
      return
      end
c
c------------------------------------------------------------------
      function psiu(zL)
c
c psiu and psit evaluate stability function for wind speed and scalars
c matching Kansas and free convection forms with weighting f
c convective form follows Fairall et al (1996) with profile constants
c from Grachev et al (2000) BLM
c stable form from Beljaars and Holtslag (1991)
c
      real*8 zL,x,y,psik,psic,f,psiu,c
      if(zL.lt.0) then
       x=(1-15.*zL)**.25                        !Kansas unstable
       psik=2.*dlog((1.+x)/2.)+dlog((1.+x*x)/2.)-2.*atan(x)+2.*atan(1.)
       y=(1.-10.15*zL)**.3333                   !Convective
       psic=1.5*dlog((1.+y+y*y)/3.)-sqrt(3.)*atan((1.+2.*y)/sqrt(3.))
     &      +4.*atan(1.)/sqrt(3.)
       f=zL*zL/(1.+zL*zL)
       psiu=(1.-f)*psik+f*psic
      else
       c=min(50.,0.35*zL)                       !Stable
       psiu=-((1.+1.*zL)**1.+.6667*(zL-14.28)/dexp(c)+8.525)
      endif
      return
      end

c--------------------------------------------------------------  
      function psit(zL)
      real*8 zL,x,y,psik,psic,f,psit,c
      if(zL.lt.0) then
       x=(1-15.*zL)**.5                          !Kansas unstable
       psik=2.*dlog((1.+x)/2.)
       y=(1.-34.15*zL)**.3333                    !Convective
       psic=1.5*dlog((1.+y+y*y)/3.)-sqrt(3.)*atan((1.+2.*y)/sqrt(3.))
     &      +4.*atan(1.)/sqrt(3.)
       f=zL*zL/(1.+zL*zL)
       psit=(1.-f)*psik+f*psic
      else
       c=min(50.,0.35*zL)                        !Stable
       psit=-((1.+2.*zL/3.)**1.5+.6667*(zL-14.28)/dexp(c)+8.525)
      endif
      return
      end
          
c-------------------------------------------------------------
      Subroutine gravity(lat,g)
c       calculates g as a function of latitude using the 1980 IUGG formula
c         
c       Bulletin Geodesique, Vol 62, No 3, 1988 (Geodesist's Handbook)
c       p 356, 1980 Gravity Formula (IUGG, H. Moritz)
c       units are in m/sec^2 and have a relative precision of 1 part
c       in 10^10 (0.1 microGal)
c       code by M. Zumberge.
c
c       check values are:
c
c        g = 9.780326772 at latitude  0.0
c        g = 9.806199203 at latitude 45.0
c        g = 9.832186368 at latitude 90.0
c
      real*8 gamma, c1, c2, c3, c4, phi, lat, g
      gamma = 9.7803267715
      c1 = 0.0052790414
      c2 = 0.0000232718
      c3 = 0.0000001262
      c4 = 0.0000000007
      phi = lat * 3.14159265358979 / 180.0
      g = gamma * (1.0 
     $ + c1 * ((sin(phi))**2)
     $ + c2 * ((sin(phi))**4)
     $ + c3 * ((sin(phi))**6)
     $ + c4 * ((sin(phi))**8))
c
      return
      end
 
 
 
 
