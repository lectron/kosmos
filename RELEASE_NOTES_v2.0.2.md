# Kosmos v2.0.2 Release Notes

## Release Date: May 3, 2026

### Overview
This maintenance release focuses on critical bug fixes and modernization for PaperSpigot servers. Two critical bugs have been resolved, Java compatibility has been upgraded to Java 11, and dependencies have been updated to support modern Minecraft versions (1.19+).

### Critical Fixes (MUST READ)
1. **UUID Mapping Bug in PlayerManager** - HIGH PRIORITY
   - Issue: getAllForServer() returned server UUIDs instead of player UUIDs
   - Impact: Player tracking across network failed
   - Fix: Changed row.getValue() to row.getKey()
   - Status: RESOLVED

2. **NullPointerException in SpawnCommands** - MEDIUM PRIORITY  
   - Issue: Console/command block execution caused crash
   - Impact: Server stability issue
   - Fix: Added missing return statement
   - Status: RESOLVED

### What's New
- **Java 11 Support**: Modern JVM features and improved performance
- **Paper 1.19.2 Compatibility**: Full support for latest PaperSpigot versions
- **Enhanced Documentation**: Comprehensive JavaDoc and code comments
- **Improved Build System**: Modern Maven configuration

### Installation & Upgrade

#### For New Installations:
```bash
git clone https://github.com/lectron/kosmos.git
cd kosmos
git checkout master
mvn clean install
```

#### For Existing Installations:
1. Backup current installation and database
2. Update code: `git pull origin master`
3. Recompile: `mvn clean install`
4. Test on development server first
5. Deploy to production

### System Requirements
- Java 11 or higher (recommended: Java 16+)
- PaperSpigot 1.19+ (1.19.2 recommended)
- Redis server (existing requirement)
- Maven 3.6+ for building

### Tested Environments
- Java 11, 16, 17 LTS
- PaperSpigot 1.19.2
- BungeeCord 1.19
- Redis 6.0+

### Breaking Changes
None - fully backward compatible with v2.0.1 data

### Migration Guide
See CHANGELOG.md for detailed version history

### Support & Contributing
- Report bugs: https://github.com/lectron/kosmos/issues
- Wiki: https://github.com/lectron/kosmos/wiki
- Contributing: See README.md

### Checksums
[Download JAR files and compute SHA-256 checksums]

### License
GNU General Public License v3.0