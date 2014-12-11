<xsl:stylesheet xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
	xmlns:dri="http://di.tamu.edu/DRI/1.0/" xmlns:mets="http://www.loc.gov/METS/"
	xmlns:xlink="http://www.w3.org/TR/xlink/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
	xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:mods="http://www.loc.gov/mods/v3"
	xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:confman="org.dspace.core.ConfigurationManager"
	xmlns="http://www.w3.org/1999/xhtml" exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc confman">


	<!-- Like the header, the footer contains various miscellaneous text, links, 
		and image placeholders -->
	<xsl:template name="buildFooter">
		<div class="row" id="cic-footer">
			<div class="col-md-2">
				<xsl:call-template name="build-anchor">
					<xsl:with-param name="a.href">
						http://www.gba.gob.ar
					</xsl:with-param>
					<xsl:with-param name="img.src">
						images/marca_para_footer.png
					</xsl:with-param>
					<xsl:with-param name="img.alt">
						BA
					</xsl:with-param>
				</xsl:call-template>
			</div>
			<div class="col-md-2 col-md-offset-1">
				<ul>

					<li>
						<xsl:call-template name="build-anchor">
							<xsl:with-param name="a.href">
								/
							</xsl:with-param>
							<xsl:with-param name="a.value">
								Inicio(i18n)
							</xsl:with-param>
						</xsl:call-template>
					</li>
					<li>
						<xsl:call-template name="build-anchor">
							<xsl:with-param name="a.href">
								/community-list
							</xsl:with-param>
							<xsl:with-param name="a.value">
								navegar (i18n)
							</xsl:with-param>
						</xsl:call-template>
					</li>
				</ul>
			</div>
			<div id="ds-footer-right">
				<address>
					<strong>Comisión de Investigaciones Científicas sbp</strong>
					<br />
					Calle 526 entre 10 y 11
					<br />
					CP: 1900 - La Plata - Buenos Aires - Argentina
					<br />
					<a href="mailto:#">info-cic@sedici.unlp.edu.ar</a>
					<br />
					<abbr title="Phone">Tel:</abbr>
					+54 (0221) 421-7374 / 482-3795
					<br />
					482-9581 / 421-3376 / 421-6205
					<br />
					<abbr title="Phone">Fax:</abbr>
					(0221) 425-8383 / 483-7999
				</address>
			</div>
		</div>
		<div class="row">
			<div>
				Este repositorio esta soportado por el software
				<a href="http://www.dspace.org/" target="_blank">DSpace</a>
			</div>
		</div>
	</xsl:template>
</xsl:stylesheet>