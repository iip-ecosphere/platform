# oktoflow platform: Tutorials

We prepared several videos on the oktoflow platform, in particular how to design and realize applications. Since the videos have been recorded, the platform evolved, i.e.,

* there were several platform releases, i.e., the version number in the videos are outdated, but you can replace the version with the most recent version as the discussed examples (mostly) use the same concepts.
* we are changing the locations of the models, in particular where the automatically obtained meta-model is located (``target/easy``, before ``src/main/easy``) and where the configuration is located (``src/main/easy``, before ``src/test/easy``)

<!--<video width="560" height="315" controls>
  <source src="https://www.youtube.com/embed/4P64iwDqqdE" type="video/mp4">
</video>-->

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/4P64iwDqqdE/0.jpg)](https://www.youtube.com/watch?v=4P64iwDqqdE)

<!--
<iframe width="560" height="315" src="https://www.youtube.com/embed/4P64iwDqqdE" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>-->


<!--
<p style="text-align: center;">Das erste Tutorial gibt einen Überblick über die IIP-Plattform basierend auf der Frage &#8220;(Wieso) Noch eine Plattform?&#8221;</p>

<div class="wpb_video_wrapper">
<iframe title="2 - IIP-Platform Tutorial – Platform Overview" width="978" height="550" src="https://www.youtube.com/embed/kbwT7u6z-Po?feature=oembed" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>


</div>
<p style="text-align: center;">Dieses Video bietet einen Überblick über das Design, die Architektur und die Services der Plattform von IIP-Ecosphere.</p>

<iframe width="560" height="315" src="https://www.youtube.com/embed/0Glp9JVxO5I" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
<p style="text-align: center;">Dieses Video erklärt, wie man den mitgelieferten Entwicklungscontainer und optional die IIP-Plattform unter Linux installiert.</p>

<iframe width="560" height="315" src="https://www.youtube.com/embed/9jnYziLD4TI" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
<p style="text-align: center;">Dieses Video erklärt, wie man den mitgelieferten Entwicklungscontainer und optional die IIP-Plattform unter Windows installiert.</p>

<iframe width="560" height="315" src="https://www.youtube.com/embed/CuyC9hvjL3M" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
<p style="text-align: center;">Dieses Video erklärt den optimalen Einsatz des Entwicklungscontainers, der im Vergleich zu Windows zunächst etwas ungewöhnlich erscheinen mag.</p>

<iframe width="560" height="315" src="https://www.youtube.com/embed/OVM11_kyqCg" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
<p style="text-align: center;">Dieses Video ist ein optionales Tutorial, das zusätzliches Wissen vermittelt und am besten vor dem ersten Video zur technischen Implementierung angesehen wird. Es vermittelt Hintergrundwissen über die Konfiguration und den Codegenerierungsansatz.</p>

<iframe width="560" height="315" src="https://www.youtube.com/embed/zeOFrOvZ9I4" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
<p style="text-align: center;">Dies ist das erste von sechs Videos zur Implementierung von Services. Es erklärt die Spezifikation der Datentypen, die zwischen den Services der Application transportiert werden müssen und markiert den Beginn des Entwicklungsprozesses.</p>

<iframe width="560" height="315" src="https://www.youtube.com/embed/Nmw92SIhycU" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
<p style="text-align: center;">Dies ist das zweite der Videos zur Service-Implementierung, in dem erklärt wird, wie Services konfiguriert werden können.</p>

<iframe width="560" height="315" src="https://www.youtube.com/embed/BL6hZQ-5B8Y" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
<p style="text-align: center;">Das dritte Video zur Service-Integration erklärt, wie Services in einer Anwendung im Sinne eines Service Mesh kombiniert werden.</p>

<iframe width="560" height="315" src="https://www.youtube.com/embed/WgGhWM-Q6D8" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
<p style="text-align: center;">Das vierte Video zur Service-Implementierung zeigt, was zu tun ist, nachdem eine Application definiert wurde. Es erklärt, wie man eine Application erstellt und wie man die Funktionalität von Services implementiert.</p>

<iframe width="560" height="315" src="https://www.youtube.com/embed/RlNmRTLM3Fs" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
<p style="text-align: center;">Im fünften Video über die Implementierung von Services geht es um das Testen der Funktionalität der zu erstellenden Application in den verschiedenen Phasen des Entwicklungsprozesses. Das Video enthält Informationen, die vor der Fertigstellung des Erstellungsprozesses aus dem vierten Video nützlich sein können, sowie Methoden, die ineinandergreifen oder für fertige Applicationen verwendet werden sollten.</p>

<iframe width="560" height="315" src="https://www.youtube.com/embed/WnFxBU0-Uuk" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
<p style="text-align: center;">Das sechste und letzte Video zur Service-Implementierung führt die Benutzer durch die Bereitstellung ihrer neuen Application auf einer Build-Plattform. Wenn Sie die Application auf einer lokalen Installation der IIP-Plattform ausführen möchten, schauen Sie bitte den entsprechenden &#8220;Platform Installation Guide&#8221; für die Installation und den Start der Plattform an.</p>

<iframe width="560" height="315" src="https://www.youtube.com/embed/leNiYPccdUw" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
<p style="text-align: center;">Dieses zusätzliche Video bezieht sich auf das zweite Video zur Service-Implementierung &#8220;How to configure Services&#8221;, da es die Unterschiede zwischen Java-basierten Services und Python-Services und deren Nutzung erklärt.</p>

<iframe width="560" height="315" src="https://www.youtube.com/embed/ObTbW869WcE" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
<p style="text-align: center;">Dieses Video gibt einen Überblick über alle Applikationsbeispiele, die derzeit mit der IIP-Plattform bereitgestellt werden. Diese Beispiele unterschiedlicher Komplexität können als Grundlage und Inspiration für eigene Anwendungen genutzt werden.</p>

<iframe width="560" height="315" src="https://www.youtube.com/embed/SaijxVtCcTg" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>
<p style="text-align: center;">Dieses Video fasst fortgeschrittene Plattformfunktionen zusammen, die in den Tutorials zur Entwicklung von Services nicht behandelt wurden. Zu den Themen gehören (externe) Konnektoren, generische Dienste, Bereitstellungspläne, Überwachung und die webbasierte Benutzeroberfläche für die Plattformverwaltung.</p>

-->
			
## Further information

For further information on using the individual parts, please consult the platform handbook linked on the main page. Please see also 

* [Windows Installation Guide](Platform_Installation_Guide_for_Windows.pdf). 
* [Linux Installation Guide](Platform_Installation_Guide_for_Linux.pdf). 
