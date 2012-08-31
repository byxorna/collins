package models
package shared

import org.specs2._
import specification._
import org.specs2.matcher._

import play.api.Configuration
import com.typesafe.config.ConfigFactory

class IpAddressConfigurationSpec extends mutable.Specification with test.ResourceFinder {

  "IpAddressConfiguration" should {

    "Provide None if given no config" in {
      IpAddressConfiguration.poolCount must_==(0)
    }

    "Provide appropriate data if no pools are specified" in new IpAddressConfigMatchers {
      val goodMap = Map(
        "network" -> "172.16.32.0/20"
      )
      val cfg = Configuration.from(goodMap)
      IpAddressConfiguration.overwriteConfig(cfg)
      val ipCfg = IpAddressConfiguration
      ipCfg must haveDefaultPool
      ipCfg.defaultPool must haveNetwork("172.16.32.0/20")
    }

    "Provide valid values when only pools are defined" in new ValidIpAddressConfig("valid_address_pools.conf") {
      config must notHaveDefaultPoolName
      pools.size === 2
      config must notHaveDefaultPool
      config must havePool("VLAN-TEST")
      config must havePool("VLAN-PROVISIONING")
      pool("VLAN-PROVISIONING") must haveStartAddress("172.16.17.10")
      pool("VLAN-PROVISIONING") must haveNetwork("172.16.17.0/24")
    }

    "Provide appropriate data when pools specified with a default" in new ValidIpAddressConfig("valid_address_pools_with_default_pool.conf") {
      config must haveDefaultPoolName("VLAN-PROVISIONING")
      pools.size === 3
      config must haveDefaultPool
      config must havePool("VLAN-TEST")
      config must havePool("VLAN-PROVISIONING")
      config must havePool("VLAN-PRODUCTION")
      config.defaultPool must haveStartAddress("172.16.17.10")
      config.defaultPool must haveNetwork("172.16.17.0/24")
      pool("VLAN-PRODUCTION") must haveNetwork("172.16.18.0/24")
    }

    "Provide appropriate data when pools specified without an explicit default" in new ValidIpAddressConfig("valid_address_pools_with_implicit_default.conf") {
      config must haveDefaultPoolName(IpAddressConfiguration.DefaultPoolName)
      config must haveDefaultPool
      config.defaultPool must haveStartAddress("172.16.15.10")
      config.defaultPool must haveNetwork("172.16.15.0/24")
    }

    "Fail if provided an invalid network" in {
      val badMap = Map(
        "network" -> "500.500.500.0/24"
      )
      val cfg = Configuration.from(badMap)
      IpAddressConfiguration.overwriteConfig(cfg)
      IpAddressConfiguration.pools must throwA[IllegalArgumentException]
    }

    "Fail if provided an invalid network/startAddress combination" in {
      val badMap = Map(
        "network" -> "10.0.0.0/24",
        "startAddress" -> "10.0.1.100"
      )
      val cfg = Configuration.from(badMap)
      IpAddressConfiguration.overwriteConfig(cfg)
      IpAddressConfiguration.pools must throwA[IllegalArgumentException]
    }

    "Fail if provided an invalid configuration" in {
      val badMap = Map( // no network, which is required
        "startAddress" -> "172.16.32.100"
      )
      val cfg = Configuration.from(badMap)
      IpAddressConfiguration.overwriteConfig(cfg)
      IpAddressConfiguration.pools must throwA[Exception].like {
        case e => e.getMessage must contain("ip_address.invalidConfig")
      }
    }

    "Fail if provided no configuration" in {
      val cfg = Configuration.from(Map(
        "pools.foo.thing" -> "",
        "pools.foo.thang" -> ""
      ))
      IpAddressConfiguration.overwriteConfig(cfg)
      IpAddressConfiguration.pools must throwA[Exception].like {
        case e => e.getMessage must contain("ip_address.missingConfig")
      }
    }

    "Fail if provided a defaultPool and no pool" in {
      val cfg = Configuration.from(Map(
        "defaultPool" -> "test",
        "pools.foo.network" -> "172.16.16.0/24"
      ))
      IpAddressConfiguration.overwriteConfig(cfg)
      IpAddressConfiguration.pools must throwA[Exception].like {
        case e => e.getMessage must contain("ip_address.strictConfig")
      }
    }
  }

  trait IpAddressConfigMatchers extends Scope {
    def haveDefaultPool: Matcher[IpAddressConfiguration] = (i:IpAddressConfiguration) => (
      i.hasDefault, "defaultPool exists", "defaultPool doest not exist"
    )
    val notHaveDefaultPool = haveDefaultPool.not

    def haveDefaultPoolName(name: String): Matcher[IpAddressConfiguration] = (i:IpAddressConfiguration) => (
      i.defaultPoolName.isDefined && i.defaultPoolName.get == name,
      "defaultPoolName exists and is %s".format(name),
      "expected defaultPoolName to be %s, was %s".format(name, i)
    )
    def notHaveDefaultPoolName: Matcher[IpAddressConfiguration] = (i:IpAddressConfiguration) => (
      !i.defaultPoolName.isDefined,
      "defaultPoolName is not defined",
      "defaultPoolName is defined"
    )

    def havePool(p: String): Matcher[IpAddressConfiguration] = (i:IpAddressConfiguration) => (
      i.hasPool(p), "have pool %s".format(p), "do not have pool %s".format(p)
    )

    def haveStartAddress(address: String): Matcher[Option[AddressPool]] = (a:Option[AddressPool]) => (
      a.isDefined && a.get.startAddress.isDefined && a.get.startAddress.get == address,
      "startAddress is %s".format(address),
      "startAddress is not %s, is %s".format(address, a)
    )
    def haveNetwork(network: String): Matcher[Option[AddressPool]] = (a:Option[AddressPool]) => (
      a.isDefined && a.get.network == network,
      "network is %s".format(network),
      "network is not %s, is %s".format(network, a)
    )

  }

  class ValidIpAddressConfig(filename: String) extends Scope with IpAddressConfigMatchers {
    val file = findResource(filename)
    val typesafeConfig = ConfigFactory.load(ConfigFactory.parseFileAnySyntax(file))
    val cfg = Configuration(typesafeConfig)
    IpAddressConfiguration.overwriteConfig(cfg)
    val config = IpAddressConfiguration
    val pools = config.pools
    def pool(p: String) = config.pool(p)
  }

}
